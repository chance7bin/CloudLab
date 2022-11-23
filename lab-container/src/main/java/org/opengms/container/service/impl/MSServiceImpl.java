package org.opengms.container.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.opengms.container.entity.bo.MsrIns;
import org.opengms.container.entity.bo.mdl.*;
import org.opengms.container.entity.dto.ModelServiceDTO;
import org.opengms.container.entity.po.ModelService;
import org.opengms.container.enums.ProcessState;
import org.opengms.container.exception.ServiceException;
import org.opengms.container.mapper.ModelServiceMapper;
import org.opengms.container.service.IMSCAsyncService;
import org.opengms.container.service.IMSInsService;
import org.opengms.container.service.IMSService;
import org.opengms.container.service.IMdlService;
import org.opengms.common.utils.StringUtils;
import org.opengms.common.utils.uuid.SnowFlake;
import org.opengms.common.utils.uuid.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    @Value(value = "${container.repository.workspace}")
    private String workspace;

    @Autowired
    IMSInsService msInsService;

    @Autowired
    IMSCAsyncService asyncService;

    @Autowired
    IMdlService mdlService;

    @Autowired
    ModelServiceMapper modelServiceMapper;

    @Override
    public void invoke(ModelService modelService) {


        // String mdlPath = "E:\\Projects\\pythonProject\\ogmslab\\test\\createWordCloud.mdl";
        String mdlPath = workspace + modelService.getRelativeDir() + modelService.getMdlFilePath();

        String exe = "python";
        // String script = "E:/Projects/pythonProject/ogmslab/test/encapsulation.py";
        // String script = workspace + modelService.getRelativeDir() + modelService.getEncapScriptPath();
        String script = "encapsulation.py";

        // 模型运行实例id
        String instanceId = UUID.fastUUID().toString();

        MsrIns msrIns = new MsrIns();
        msrIns.setMsriId(instanceId);
        msrIns.setCurrentState(ProcessState.INIT);
        msrIns.setModelService(modelService);
        // msrIns.setModelClass(modelService.getModelClass());

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

        // TODO: 2022/11/15 运行容器的路径与宿主机之间的映射
        cmdArr = new String[] {"docker", "exec", "jupyter_cus_5.0_8268889755334766592", "/bin/bash", "-c", pythonCMD};
        // String[] cmdArr = new String[] {exe, "-m", script, MSC_HOST, MSC_PORT, instanceId};

        asyncService.exec(cmdArr);

        // 调用成功之后
        // if (exitVal == 0){
        //     Map<String, MsrIns> msrInsColl = msInsService.getMsrInsColl();
        //     msrInsColl.put(instanceId,msrIns);
        // }

    }

    @Override
    public int insertModelService(ModelServiceDTO modelServiceDTO) {

        // TODO: 2022/11/12 服务关联的文件夹暂时用containerId对应的文件夹
        // TODO: 2022/11/12 模型服务暂时不创建新的镜像，暂时先用工作空间的镜像
        String workspaceDir = "/workspace/" + modelServiceDTO.getContainerName();
        modelServiceDTO.setRelativeDir(workspaceDir);

        ModelService modelService = new ModelService();
        modelService.setMsId(SnowFlake.nextId());
        BeanUtils.copyProperties(modelServiceDTO,modelService);
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
        if (service != null){
            if (StringUtils.isNull(service.getModelClass())){
                // 如果没有mdl对象则解析mdl文件

                try {
                    // 解析mdl文档
                    String mdlPath = workspace + service.getRelativeDir() + service.getMdlFilePath();
                    ModelClass modelClass = mdlService.parseMdlFile(mdlPath);
                    service.setModelClass(modelClass);

                    // mdl解析成字符串存入数据库中
                    modelServiceMapper.updateModelService(service);

                    // System.out.println();
                } catch (Exception e){
                    log.error("解析mdl出错: " + e.getMessage());
                    throw new ServiceException("解析mdl出错: " + e.getMessage());
                }
            }
        }

        return service;
    }


}
