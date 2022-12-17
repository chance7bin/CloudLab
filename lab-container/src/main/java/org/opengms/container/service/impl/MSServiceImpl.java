package org.opengms.container.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import lombok.extern.slf4j.Slf4j;
import org.opengms.common.TerminalRes;
import org.opengms.common.utils.DateUtils;
import org.opengms.container.constant.ContainerConstants;
import org.opengms.container.constant.TaskStatus;
import org.opengms.container.entity.dto.docker.JupyterInfoDTO;
import org.opengms.container.entity.po.MsrIns;
import org.opengms.container.entity.bo.mdl.*;
import org.opengms.container.entity.dto.ModelServiceDTO;
import org.opengms.container.entity.po.ModelService;
import org.opengms.container.entity.po.docker.ContainerInfo;
import org.opengms.container.entity.po.docker.JupyterContainer;
import org.opengms.container.enums.ProcessState;
import org.opengms.container.exception.ServiceException;
import org.opengms.container.mapper.DockerOperMapper;
import org.opengms.container.mapper.ModelServiceMapper;
import org.opengms.container.mapper.MsrInsMapper;
import org.opengms.container.service.*;
import org.opengms.common.utils.StringUtils;
import org.opengms.common.utils.uuid.SnowFlake;
import org.opengms.common.utils.uuid.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 7bin
 * @date 2022/11/07
 */
@Slf4j
@Service
public class MSServiceImpl implements IMSService {

    private final static String MSC_HOST = "172.21.212.240";

    private final static String MSC_PORT = "6001";

    @Value(value = "${container.repository}")
    private String repository;

    @Autowired
    IMSInsService msInsService;

    @Autowired
    IMSCAsyncService asyncService;

    @Autowired
    IMdlService mdlService;

    @Autowired
    ModelServiceMapper modelServiceMapper;

    @Autowired
    DockerOperMapper dockerOperMapper;

    @Autowired
    MsrInsMapper msrInsMapper;

    @Autowired
    IDockerService dockerService;

    @Autowired
    IWorkspaceService workspaceService;

    @Override
    public String invoke(ModelService modelService) {

        ContainerInfo containerInfo = null;
        if (modelService.getNewImage() != null && modelService.getNewImage()){
            // 使用新环境创建的服务要先创建并启动容器
            containerInfo = invokePrepare(modelService);
        }

        String exe = "python";
        // String script = "E:/Projects/pythonProject/ogmslab/test/encapsulation.py";
        // String script = workspace + modelService.getRelativeDir() + modelService.getEncapScriptPath();

        // 调用的封装脚本和mdl放在 serviceDir 的 ${msName}_${msId}/model 文件夹下
        String script;
        if (StringUtils.isEmpty(modelService.getRelativeDir())){
            script = ContainerConstants.INNER_SERVICE_DIR + "/model" + modelService.getEncapScriptPath();
        } else {
            script = ContainerConstants.INNER_SERVICE_DIR + "/" + modelService.getRelativeDir() + "/model" + modelService.getEncapScriptPath();
        }
        // String mdlPath = "E:\\Projects\\pythonProject\\ogmslab\\test\\createWordCloud.mdl";
        String mdlPath = ContainerConstants.INNER_SERVICE_DIR + "/" + modelService.getRelativeDir() + "/model" + modelService.getMdlFilePath();

        // 模型运行实例id
        String instanceId = UUID.fastUUID().toString();

        MsrIns msrIns = new MsrIns();
        msrIns.setMsriId(instanceId);
        msrIns.setCurrentState(ProcessState.INIT);
        msrIns.setModelService(modelService);
        msrIns.setMsId(modelService.getMsId());
        msrIns.setStatus(TaskStatus.INIT);
        if (modelService.getContainerId() != null){
            msrIns.setContainerId(modelService.getContainerId());
        } else {
            if (containerInfo == null || containerInfo.getContainerId() == null){
                throw new ServiceException("运行容器创建出错");
            }
            msrIns.setContainerId(containerInfo.getContainerId());
        }
        // msrIns.setModelClass(modelService.getModelClass());

        msrInsMapper.insertMsrIns(msrIns);

        // 解析mdl文档
        // try {
        //     ModelClass modelClass = mdlService.parseMdlFile(mdlPath);
        //
        //     // 设置临时输入数据
        //     modelClass.getBehavior().getStateGroup().getStates().get(0).getEvents().get(0)
        //         .getInputParameter().setValue("English");
        //     modelClass.getBehavior().getStateGroup().getStates().get(0).getEvents().get(1)
        //         .getInputParameter().setValue("E:\\opengms-lab\\container\\workspace\\jupyter_cus_5.0_8268889755334766592\\hamlet.txt");
        //
        //     msrIns.getModelService().setModelClass(modelClass);
        // } catch (Exception e){
        //     log.error("解析mdl出错: " + e.getMessage());
        //     throw new ServiceException("解析mdl出错: " + e.getMessage());
        // }

        // 将该实例绑定到实例集合中
        Map<String, MsrIns> msrInsColl = msInsService.getMsrInsColl();
        msrInsColl.put(instanceId,msrIns);

        // python执行命令要添加-m参数才能把python代码所在路径加入到sys.path中
        // docker exec $DOCKER_ID /bin/bash -c 'cd /packages/detectron && python tools/train.py'
        // String[] cmdArr = new String[] {exe, script, MSC_HOST, MSC_PORT, instanceId};
        String[] cmdArr = new String[] {exe, script, MSC_HOST, MSC_PORT, instanceId};
        String pythonCMD = String.join(" ", cmdArr);

        String containerName;
        if (msrIns.getModelService().getContainerId() != null){
            // jupyter发布的服务
            JupyterContainer container = dockerOperMapper.getJupyterContainerInfoById(msrIns.getModelService().getContainerId());
            containerName = container.getContainerName();
        } else {
            if (containerInfo == null){
                throw new ServiceException("容器创建失败");
            }
            containerName = containerInfo.getContainerName();
        }
        cmdArr = new String[] {"docker", "exec", containerName, "/bin/bash", "-c", pythonCMD};
        // String[] cmdArr = new String[] {exe, "-m", script, MSC_HOST, MSC_PORT, instanceId};

        asyncService.exec(cmdArr);

        // 调用成功之后
        // if (exitVal == 0){
        //     Map<String, MsrIns> msrInsColl = msInsService.getMsrInsColl();
        //     msrInsColl.put(instanceId,msrIns);
        // }

        return instanceId;

    }

    // 使用新环境创建的服务 容器没启动前要先启动容器, 之后再调用
    private ContainerInfo invokePrepare(ModelService modelService){
        // 创建容器并启动


        // 初始化jupyter实例
        ContainerInfo containerInfo= new ContainerInfo();
        containerInfo.setContainerId(SnowFlake.nextId());
        containerInfo.setImageName(modelService.getImageName());
        containerInfo.setContainerName(modelService.getMsName() + "_" + System.currentTimeMillis());

        // 创建者为该登录用户
        // containerInfo.setCreateBy(username);

        // 设置模型运行服务路径
        String serviceDir = "/service/" + modelService.getMsName() + "_" + modelService.getNewPkgId();

        List<String> volumeList = containerInfo.getVolumeList();
        volumeList.add(serviceDir + ":" + ContainerConstants.INNER_SERVICE_DIR);

        //对创建容器抛出的异常做处理
        try {
            // 启动容器
            containerInfo.setCmd(ContainerConstants.RUN_DEFAULT_CMD);
            dockerService.createContainer(containerInfo);
        } catch (InternalServerErrorException serverErrorException){
            if (serverErrorException.getMessage().contains("port is already allocated")){
                throw new ServiceException("端口被占用");
            }
            else {
                throw new ServiceException(serverErrorException.getMessage());
            }
        } catch (Exception e){
            throw new ServiceException(e.getMessage());
        }

        int count = dockerOperMapper.insertContainer(containerInfo);

        if (count <= 0){
            throw new ServiceException("创建容器失败");
        }

        return containerInfo;


    }

    @Override
    public int insertModelService(ModelServiceDTO modelServiceDTO) {

        // TODO: 2022/11/12 服务关联的文件夹暂时用containerId对应的文件夹
        // TODO: 2022/11/12 模型服务暂时不创建新的镜像，暂时先用工作空间的镜像
        // String workspaceDir = "/workspace/" + modelServiceDTO.getContainerName();
        // modelServiceDTO.setRelativeDir(workspaceDir);

        ModelService modelService = new ModelService();
        modelService.setMsId(SnowFlake.nextId());
        BeanUtils.copyProperties(modelServiceDTO,modelService);
        modelService.setRelativeDir(modelService.getMsName() + "_" + modelService.getMsId());


        //拷贝该工作空间的文件至service的model文件夹下
        FileUtil.copyContent(
            new File(repository + ContainerConstants.workspaceDir(modelService.getContainerId())),
            new File(repository + ContainerConstants.serviceDir(modelService.getContainerId()) + "/" + modelService.getRelativeDir() + "/model"),
            false
        );

        // 把封装脚本和mdl拷贝至 relativeDir 的 model 文件夹中
        // TODO: 2022/12/8 当前封装脚本只支持从容器内部选择文件，暂不支持从Drive中选择
        // if (xx.contaian(".py" || ".mdl")) 就是从容器中的 如果不是就是从Drive的 就需要下载(前端要对输入的文件类型做判断)

        FileUtil.copy(
            new File(repository + ContainerConstants.workspaceDir(modelService.getContainerId()) + modelServiceDTO.getEncapScriptPath()),
            new File(repository + ContainerConstants.serviceDir(modelService.getContainerId()) + "/" + modelService.getRelativeDir() + "/model" + modelServiceDTO.getEncapScriptPath()),
            true
        );
        FileUtil.copy(
            new File(repository + ContainerConstants.workspaceDir(modelService.getContainerId()) + modelServiceDTO.getMdlFilePath()),
            new File(repository + ContainerConstants.serviceDir(modelService.getContainerId()) + "/" + modelService.getRelativeDir() + "/model" + modelServiceDTO.getMdlFilePath()),
            true
        );

        // 解析mdl
        try {
            // 解析mdl文档
            // String workspaceVolume = container.getWorkspaceVolume();
            // String[] split = workspaceVolume.split(":");
            // String workspaceDir = split[0];
            // String mdlPath = repository + workspaceDir + service.getMdlFilePath();
            String mdlPath = repository + ContainerConstants.serviceDir(modelService.getContainerId()) + "/" + modelService.getRelativeDir() + "/model" + modelService.getMdlFilePath();
            ModelClass modelClass = mdlService.parseMdlFile(mdlPath);
            modelService.setModelClass(modelClass);
        } catch (Exception e){
            log.error("解析mdl出错: " + e.getMessage());
            throw new ServiceException("解析mdl出错: " + e.getMessage());
        }

        // 新环境
        if (modelServiceDTO.getNewImage() != null && modelServiceDTO.getNewImage()){

            asyncService.createNewEnv(modelService);

        }

        return modelServiceMapper.insertModelService(modelService);


    }



    @Override
    public List<ModelService> selectServiceList() {
        return modelServiceMapper.selectServiceList();
    }

    @Override
    public ModelService getModelServiceById(String msId) {

        //根据mdl的文件路径返回mdl对象，并更新到数据库中
        ModelService service = modelServiceMapper.getModelServiceById(msId);

        // Long containerId = service.getContainerId();
        // JupyterContainer container = dockerOperMapper.getJupyterContainerInfoById(containerId);

        // 解析mdl在创建服务的时候就要解析了
        // if (service != null){
        //     if (StringUtils.isNull(service.getModelClass())){
        //         // 如果没有mdl对象则解析mdl文件
        //
        //         try {
        //             // 解析mdl文档
        //             // String workspaceVolume = container.getWorkspaceVolume();
        //             // String[] split = workspaceVolume.split(":");
        //             // String workspaceDir = split[0];
        //             // String mdlPath = repository + workspaceDir + service.getMdlFilePath();
        //             String mdlPath = repository + ContainerConstants.serviceDir(service.getContainerId()) + "/" + service.getRelativeDir() + "/model" + service.getMdlFilePath();
        //             ModelClass modelClass = mdlService.parseMdlFile(mdlPath);
        //             service.setModelClass(modelClass);
        //
        //             // mdl解析成字符串存入数据库中
        //             modelServiceMapper.updateModelService(service);
        //
        //             // System.out.println();
        //         } catch (Exception e){
        //             log.error("解析mdl出错: " + e.getMessage());
        //             throw new ServiceException("解析mdl出错: " + e.getMessage());
        //         }
        //     }
        // }

        return service;
    }


}
