package org.opengms.admin.msc.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.opengms.admin.exception.ServiceException;
import org.opengms.admin.msc.entity.bo.MsrIns;
import org.opengms.admin.msc.entity.bo.mdl.*;
import org.opengms.admin.msc.enums.MDLStructure;
import org.opengms.admin.msc.enums.ProcessState;
import org.opengms.admin.msc.service.IMSCAsyncService;
import org.opengms.admin.msc.service.IMSInsService;
import org.opengms.admin.msc.service.IMSService;
import org.opengms.admin.msc.service.IMdlService;
import org.opengms.common.utils.ReflectUtils;
import org.opengms.common.utils.uuid.UUID;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    IMSInsService msInsService;

    @Autowired
    IMSCAsyncService asyncService;

    @Autowired
    IMdlService mdlService;

    @Override
    public void invoke() {

        String mdlPath = "E:\\Projects\\pythonProject\\ogmslab\\test\\createWordCloud.mdl";

        String exe = "python";
        String script = "E:/Projects/pythonProject/ogmslab/test/encapsulation.py";

        // 模型运行实例id
        String instanceId = UUID.fastUUID().toString();

        MsrIns msrIns = new MsrIns();
        msrIns.setMsriId(instanceId);
        msrIns.setCurrentState(ProcessState.INIT);


        // 解析mdl文档
        try {
            ModelClass modelClass = mdlService.parseMdlFile(mdlPath);

            // 设置临时输入数据
            modelClass.getBehavior().getStateGroup().getStates().get(0).getEvents().get(0)
                .getInputParameter().setTextValue("English");
            modelClass.getBehavior().getStateGroup().getStates().get(0).getEvents().get(1)
                .getInputParameter().setFilePath("E:\\opengms-lab\\container\\workspace\\jupyter_cus_5.0_8268889755334766592\\hamlet.txt");

            msrIns.setModelClass(modelClass);
        } catch (Exception e){
            log.error("解析mdl出错: " + e.getMessage());
            throw new ServiceException("解析mdl出错: " + e.getMessage());
        }

        // 将该实例绑定到实例集合中
        Map<String, MsrIns> msrInsColl = msInsService.getMsrInsColl();
        msrInsColl.put(instanceId,msrIns);

        String[] cmdArr = new String[] {exe, script, MSC_HOST, MSC_PORT, instanceId};

        asyncService.exec(cmdArr);

        // 调用成功之后
        // if (exitVal == 0){
        //     Map<String, MsrIns> msrInsColl = msInsService.getMsrInsColl();
        //     msrInsColl.put(instanceId,msrIns);
        // }

    }







}
