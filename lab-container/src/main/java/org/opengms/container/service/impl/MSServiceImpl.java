package org.opengms.container.service.impl;

import cn.hutool.core.io.FileUtil;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import lombok.extern.slf4j.Slf4j;
import org.opengms.container.constant.*;
import org.opengms.container.entity.dto.InvokeDTO;
import org.opengms.container.entity.po.JupyterContainer;
import org.opengms.container.entity.po.MsrIns;
import org.opengms.container.entity.bo.mdl.*;
import org.opengms.container.entity.dto.ModelServiceDTO;
import org.opengms.container.entity.po.ModelService;
import org.opengms.container.entity.po.docker.ContainerInfo;
import org.opengms.container.entity.po.docker.ImageInfo;
import org.opengms.container.enums.ContainerType;
import org.opengms.container.enums.ProcessState;
import org.opengms.container.exception.ServiceException;
import org.opengms.container.mapper.ImageMapper;
import org.opengms.container.mapper.ModelServiceMapper;
import org.opengms.container.mapper.MsrInsMapper;
import org.opengms.container.service.*;
import org.opengms.common.utils.uuid.SnowFlake;
import org.opengms.common.utils.uuid.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
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
    IContainerService containerService;

    @Autowired
    MsrInsMapper msrInsMapper;

    @Autowired
    IDockerService dockerService;

    @Autowired
    ImageMapper imageMapper;

    @Autowired
    IWorkspaceService workspaceService;

    @Override
    public String invoke(InvokeDTO invokeDTO) {

        ModelService modelService = modelServiceMapper.selectById(invokeDTO.getMsId());

        // 调用服务前需先判断服务是否部署成功，
        if (!DeployStatus.FINISHED.equals(modelService.getDeployStatus())){
            throw new ServiceException("服务未部署成功，无法调用");
        }

        modelService.setModelClass(invokeDTO.getModelClass());

        // 调用服务前，服务环境容器的准备
        ContainerInfo containerInfo = invokePrepare(modelService);


        String exe = "python";
        // String script = "E:/Projects/pythonProject/ogmslab/test/encapsulation.py";
        // String script = workspace + modelService.getRelativeDir() + modelService.getEncapScriptPath();

        // 模型服务相关文件放在 /service/{containerId}/{msId} 下
        // String script ;
        // if (StringUtils.isEmpty(modelService.getRelativeDir())){
        //     script = ContainerConstants.INNER_SERVICE_DIR + "/model" + modelService.getEncapScriptPath();
        // } else {
        //     script = ContainerConstants.INNER_SERVICE_DIR + "/" + modelService.getRelativeDir() + "/model" + modelService.getEncapScriptPath();
        // }
        String script = ContainerConstants.INNER_SERVICE_DIR + "/model" + modelService.getEncapScriptPath();

        // 模型运行实例id
        String instanceId = UUID.fastUUID().toString();

        MsrIns msrIns = new MsrIns();
        msrIns.setMsriId(instanceId);
        msrIns.setCurrentState(ProcessState.INIT);
        msrIns.setModelService(modelService);
        msrIns.setMsId(modelService.getMsId());
        msrIns.setStatus(TaskStatus.INIT);
        // if (modelService.getContainerId() != null){
        //     msrIns.setContainerId(modelService.getContainerId());
        // } else {
        //     if (containerInfo == null || containerInfo.getContainerId() == null){
        //         throw new ServiceException("运行容器创建出错");
        //     }
        //     msrIns.setContainerId(containerInfo.getContainerId());
        // }
        // msrIns.setModelClass(modelService.getModelClass());
        msrIns.setContainerId(containerInfo.getContainerId());

        msrInsMapper.insert(msrIns);

        // 将该实例绑定到实例集合中
        Map<String, MsrIns> msrInsColl = msInsService.getMsrInsColl();
        msrInsColl.put(instanceId,msrIns);

        // python执行命令要添加-m参数才能把python代码所在路径加入到sys.path中
        // docker exec $DOCKER_ID /bin/bash -c 'cd /packages/detectron && python tools/train.py'
        // String[] cmdArr = new String[] {exe, script, MSC_HOST, MSC_PORT, instanceId};
        String[] cmdArr = new String[] {exe, script, MSC_HOST, MSC_PORT, instanceId};
        String pythonCMD = String.join(" ", cmdArr);

        String containerName = containerInfo.getContainerName();
        // if (msrIns.getModelService().getContainerId() != null){
        //     // jupyter发布的服务
        //     ContainerInfo container = containerService.getContainerInfoById(msrIns.getModelService().getContainerId(), ContainerType.JUPYTER);
        //     containerName = container.getContainerName();
        // } else {
        //     if (containerInfo == null){
        //         throw new ServiceException("容器创建失败");
        //     }
        //     containerName = containerInfo.getContainerName();
        // }
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

    // 调用前准备 根据关联image启动容器, 之后再调用
    private ContainerInfo invokePrepare(ModelService modelService){

        // 如果模型服务不是由新环境创建的，但是该container没有启动，则要先启动该容器
        // if (!modelService.getNewEnv()){
        //     // 容器没启动的话先启动容器
        //     ContainerInfo container = containerService.getContainerInfoById(modelService.getContainerId(), ContainerType.JUPYTER);
        //     if (!ContainerStatus.RUNNING.equals(container.getStatus())){
        //         dockerService.startContainer(container.getContainerInsId());
        //     }
        //     return container;
        // }


        // 如果模型服务是由新环境创建的，说明该服务的运行容器是根据 imageTar 动态生成的

        // 创建容器并启动

        // 初始化jupyter实例
        ContainerInfo containerInfo= new JupyterContainer();
        containerInfo.setContainerId(SnowFlake.nextId());
        containerInfo.setImageId(modelService.getImageId());
        ImageInfo imageInfo = imageMapper.selectById(modelService.getImageId());
        containerInfo.setImageName(imageInfo.getRepoTags());
        containerInfo.setContainerName(modelService.getMsName() + "_" + System.currentTimeMillis());

        // 创建者为该登录用户
        // containerInfo.setCreateBy(username);

        // 设置模型运行服务路径: 模型运行所需的文件在创建service时就已经迁移到 /service/{msId} 路径下了
        // 路径为 /service/{msId}
        String serviceDir = ContainerConstants.SERVICE_DIR(modelService.getMsId());;
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
            throw new ServiceException(e.toString());
        }

        int count = containerService.insertContainer(containerInfo, ContainerType.JUPYTER);

        if (count <= 0){
            throw new ServiceException("创建容器失败");
        }

        return containerInfo;


    }

    @Override
    public int insertModelService(ModelServiceDTO modelServiceDTO) {

        // TODO: 2022/11/12 服务关联的文件夹暂时用containerId对应的文件夹
        // String workspaceDir = "/workspace/" + modelServiceDTO.getContainerName();
        // modelServiceDTO.setRelativeDir(ContainerConstants.DATA_DIR(modelServiceDTO.getContainerId()));

        ModelService modelService = new ModelService();
        modelService.setMsId(SnowFlake.nextId());
        BeanUtils.copyProperties(modelServiceDTO,modelService);

        String serviceDir = ContainerConstants.SERVICE_DIR(modelService.getMsId());
        String serviceModelDir = serviceDir + "/model";
        //拷贝该工作空间的文件至service/{msId}/model文件夹下
        FileUtil.copyContent(
            new File(repository + ContainerConstants.DATA_DIR(modelService.getContainerId()) + modelServiceDTO.getRelativeDir()),
            new File(repository + serviceModelDir),
            false
        );

        // 把封装脚本和mdl拷贝至 relativeDir 的 model 文件夹中
        // TODO: 2022/12/8 当前封装脚本只支持从容器内部选择文件，暂不支持从Drive中选择
        // if (xx.contaian(".py" || ".mdl")) 就是从容器中的 如果不是就是从Drive的 就需要下载(前端要对输入的文件类型做判断)

        FileUtil.copy(
            new File(repository + ContainerConstants.DATA_DIR(modelService.getContainerId()) + modelService.getEncapScriptPath()),
            new File(repository + serviceModelDir + modelService.getEncapScriptPath()),
            true
        );
        FileUtil.copy(
            new File(repository + ContainerConstants.DATA_DIR(modelService.getContainerId()) + modelService.getMdlFilePath()),
            new File(repository + serviceModelDir + modelService.getMdlFilePath()),
            true
        );

        // 解析mdl
        try {
            // 解析mdl文档
            // String workspaceVolume = container.getWorkspaceVolume();
            // String[] split = workspaceVolume.split(":");
            // String workspaceDir = split[0];
            // String mdlPath = repository + workspaceDir + service.getMdlFilePath();
            String mdlPath = repository + serviceModelDir + modelService.getMdlFilePath();
            // ModelClass modelClass = mdlService.parseMdlFile(mdlPath);
            ModelClass modelClass = mdlService.parseMdlFileWithAnnotation(mdlPath);
            if (modelClass == null){
                throw new ServiceException("!");
            }
            modelService.setModelClass(modelClass);
        } catch (Exception e){
            log.error("解析mdl出错: " + e.getMessage());
            throw new ServiceException("解析mdl出错: " + e.getMessage());
        }

        // 新环境
        // if (modelServiceDTO.getNewEnv() != null && modelServiceDTO.getNewEnv()){
        //     modelService.setDeployStatus(DeployStatus.INIT);
        //     try {
        //         asyncService.createNewEnv(modelService);
        //     }catch (ServiceException e){
        //         modelServiceMapper.updateDeployStatus(modelService.getMsId(), DeployStatus.ERROR);
        //         throw new ServiceException("创建容器失败");
        //     }
        //
        // } else {
        //     modelService.setDeployStatus(DeployStatus.FINISHED);
        // }

        modelService.setDeployStatus(DeployStatus.FINISHED);

        return modelServiceMapper.insert(modelService);

    }



    @Override
    public List<ModelService> selectServiceList() {
        return modelServiceMapper.selectList();
    }

    @Override
    public ModelService getModelServiceById(String msId) {

        //根据mdl的文件路径返回mdl对象，并更新到数据库中
        ModelService service = modelServiceMapper.selectById(Long.valueOf(msId));

        // Long containerId = service.getContainerId();
        // JupyterContainer container = dockerOperMapper.getJupyterContainerInfoById(containerId);

        return service;
    }



}
