package org.opengms.container.service.impl;

import cn.hutool.core.io.FileUtil;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import com.github.dockerjava.api.model.Bind;
import lombok.extern.slf4j.Slf4j;
import org.opengms.container.constant.*;
import org.opengms.container.entity.bo.InOutParam;
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

    @Value(value = "${mscSocket.host}")
    private String MSC_HOST;

    @Value(value = "${mscSocket.port}")
    private String MSC_PORT;

    @Value(value = "${container.repository}")
    private String repository;

    @Value(value = "${modelService.mode}")
    private String serviceMode;

    @Value(value = "${k8s.namespace}")
    private String namespace;

    @Autowired
    IMSInsSocketService msInsSocketService;

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
    IK8sService k8sService;

    @Autowired
    ImageMapper imageMapper;

    @Autowired
    IWorkspaceService workspaceService;

    @Override
    public String invoke(InvokeDTO invokeDTO) {

        // TODO: 2023/4/25 并发调用同一个服务，如何解决资源冲突问题

        ModelService modelService = modelServiceMapper.selectById(invokeDTO.getMsId());

        // 调用服务前需先判断服务是否部署成功，
        if (!DeployStatus.FINISHED.equals(modelService.getDeployStatus())){
            throw new ServiceException("服务未部署成功，无法调用");
        }

        modelService.setModelClass(invokeDTO.getModelClass());

        // 调用服务前，服务环境容器的准备
        ContainerInfo containerInfo = invokePrepare(modelService);
        String containerName = containerInfo.getContainerName();

        // 创建模型运行实例
        // 模型运行实例id
        String instanceId = UUID.fastUUID().toString();
        MsrIns msrIns = new MsrIns();
        msrIns.setMsriId(instanceId);
        msrIns.setCurrentState(ProcessState.INIT);
        msrIns.setModelService(modelService);
        msrIns.setMsId(modelService.getMsId());
        msrIns.setStatus(TaskStatus.INIT);
        msrIns.setContainerId(containerInfo.getContainerId());
        // 构建input数据
        msrIns.setInputs(buildInputs(modelService));
        msrInsMapper.insert(msrIns);
        // 将该实例绑定到实例集合中
        Map<String, MsrIns> msrInsColl = msInsSocketService.getMsrInsColl();
        msrInsColl.put(instanceId,msrIns);

        // 构建python执行命令
        String exe = "python";
        // String script = "E:/Projects/pythonProject/ogmslab/test/encapsulation.py";
        // String script = workspace + modelService.getRelativeDir() + modelService.getEncapScriptPath();
        String script = ContainerConstants.INNER_SERVICE_DIR + "/model" + modelService.getEncapScriptPath();
        // python执行命令要添加-m参数才能把python代码所在路径加入到sys.path中
        // docker exec $DOCKER_ID /bin/bash -c 'cd /packages/detectron && python tools/train.py'
        // String[] cmdArr = new String[] {exe, script, MSC_HOST, MSC_PORT, instanceId};
        String[] cmdArr = new String[] {exe, script, MSC_HOST, MSC_PORT, instanceId};

        // docker的调用命令
        if (ServiceMode.DOCKER.equals(serviceMode)){
            String cmdStr = String.join(" ", cmdArr);
            cmdArr = new String[] {"docker", "exec", containerName, "/bin/bash", "-c", cmdStr};
            asyncService.exec(cmdArr);
        } else{

            // 检查pod是否已经创建好了

            // TODO: 2023/5/15 如果没有这个镜像要先到hub上拉，这个耗时可能会超过10s
            
            int cnt = 0;
            while (true){
                String status = k8sService.getPodStatus(containerName, namespace);
                if ("Running".equals(status)){
                    break;
                }
                if (cnt > 10){
                    // 删除对应的模型运行实例
                    msrInsColl.remove(instanceId);
                    throw new ServiceException("pod创建超时");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("waiting for pod to be ready, current status: " + status);
                cnt++;
            }
            log.info("start run model service!");

            // pod 中的container name
            String podContainerName = k8sService.getPodContainerName(containerName);

            asyncService.exec(namespace, containerName, podContainerName, cmdArr);
        }

        // 调用成功之后
        // if (exitVal == 0){
        //     Map<String, MsrIns> msrInsColl = msInsService.getMsrInsColl();
        //     msrInsColl.put(instanceId,msrIns);
        // }

        return instanceId;

    }

    // 构建前端传入的input数据
    private List<InOutParam> buildInputs(ModelService modelService) {

        List<InOutParam> inputs = new ArrayList<>();

        // add(new InOutParam(state, event, dataMIME.getInfo(), parameter))

        ModelClass modelClass = modelService.getModelClass();

        List<State> states = modelClass.getBehavior().getStateGroup().getStates();

        for (State state : states) {
            List<Event> events = state.getEvents();
            for (Event event : events) {
                InputParameter inputParameter = event.getInputParameter();
                if (inputParameter != null){
                    inputs.add(new InOutParam(state.getName(), event.getName(), inputParameter.getDataMIME(), inputParameter.getValue()));
                }
            }
        }

        return inputs;
    }

    // 调用前准备 根据关联image启动容器, 之后再调用
    private ContainerInfo invokePrepare(ModelService modelService){

        // 创建容器并启动

        // 初始化jupyter实例
        ContainerInfo containerInfo= new JupyterContainer();
        containerInfo.setContainerId(SnowFlake.nextId());
        containerInfo.setImageId(modelService.getImageId());
        ImageInfo imageInfo = imageMapper.selectById(modelService.getImageId());
        containerInfo.setImageName(dockerService.getRealImageNameWithTag(imageInfo.getRepoTags()));
        containerInfo.setContainerName(modelService.getMsName() + "-" + System.currentTimeMillis());

        // 创建者为该登录用户
        // containerInfo.setCreateBy(username);

        // 设置模型运行服务路径: 模型运行所需的文件在创建service时就已经迁移到 /service/{msId} 路径下了
        // 路径为 /service/{msId}
        String serviceDir = ContainerConstants.SERVICE_DIR(modelService.getMsId());;
        List<String> volumeList = containerInfo.getVolumeList();
        volumeList.add(serviceDir + ":" + ContainerConstants.INNER_SERVICE_DIR);

        int count = containerService.insertContainer(containerInfo, ContainerType.JUPYTER);

        if (count <= 0){
            throw new ServiceException("插入容器信息失败");
        }

        if (ServiceMode.DOCKER.equals(serviceMode)){
            prepareContainerByDocker(containerInfo);
        } else {
            // 模型服务使用k8s创建容器
            prepareContainerByK8s(containerInfo);
        }

        return containerInfo;


    }

    // 调用前准备，先启动容器，再调用
    private void prepareContainerByDocker(ContainerInfo containerInfo){

        ImageInfo imageInfo = imageMapper.selectById(containerInfo.getImageId());

        // 启动容器前需先判断该镜像是否已经在本地
        try {
            dockerService.inspectImage(imageInfo.getImageId());
        } catch (Exception ex){
            // docker中没有该image到hub上拉
            try {
                log.info("从docker hub中拉取镜像: " + imageInfo.getImageName() + ":" + imageInfo.getTag());
                dockerService.pullImage(imageInfo.getImageName(), imageInfo.getTag());
            } catch (InterruptedException e) {
                throw new ServiceException("拉取镜像失败");
            }
        }

        //对创建容器抛出的异常做处理
        try {
            // 启动容器
            containerInfo.setCmd(ContainerConstants.RUN_DEFAULT_CMD);

            // 补全volume信息
            List<String> volumeList = containerInfo.getVolumeList();
            for (int i = 0; i < volumeList.size(); i++) {
                volumeList.set(i, repository + volumeList.get(i));
            }

            dockerService.createContainer(containerInfo);
            containerService.updateContainerInsId(containerInfo.getContainerId(), containerInfo.getContainerInsId(), ContainerType.JUPYTER);
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

    }

    private void prepareContainerByK8s(ContainerInfo containerInfo){

        k8sService.createPod(containerInfo.getContainerName(), namespace, containerInfo.getImageName(), containerInfo.getVolumeList());

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
