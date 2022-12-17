package org.opengms.container.service.impl;

import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.opengms.common.utils.uuid.UUID;
import org.opengms.container.clients.DriveClient;
import org.opengms.container.constant.ContainerConstants;
import org.opengms.container.entity.bo.mdl.*;
import org.opengms.container.entity.dto.ApiResponse;
import org.opengms.container.entity.po.ModelService;
import org.opengms.container.enums.DataMIME;
import org.opengms.container.enums.MDLStructure;
import org.opengms.container.exception.ServiceException;
import org.opengms.container.service.IMdlService;
import org.opengms.common.utils.ReflectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author 7bin
 * @date 2022/11/07
 */
@Service
@Slf4j
public class MdlServiceImpl implements IMdlService {

    @Value(value = "${labDriveUrl}")
    String labDriveUrl;

    @Autowired
    DriveClient driveClient;

    @Value(value = "${container.repository}")
    private String repository;

    @Override
    public ModelClass parseMdlFile(String mdlFilePath) throws Exception {
        SAXReader saxReader = new SAXReader();

        Document document = saxReader.read(new File(mdlFilePath));

        // 获取ModelClass
        ModelClass modelClass = new ModelClass();
        Element modelClassNode = document.getRootElement();
        setElementAttributes(modelClassNode.attributes(), modelClass);

        // 获取AttributeSet
        AttributeSet attributeSet = new AttributeSet();
        Element attributeSetNode = modelClassNode.element(MDLStructure.ATTRIBUTE_SET.getInfo());
        Description description = new Description();
        Element descriptionNode = attributeSetNode.element(MDLStructure.DESCRIPTION.getInfo());
        setElementAttributes(descriptionNode.attributes(), description);
        attributeSet.setDescription(description);
        modelClass.setAttributeSet(attributeSet);

        // 获取Behavior
        Behavior behavior = new Behavior();
        Element behaviorNode = modelClassNode.element(MDLStructure.BEHAVIOR.getInfo());
        StateGroup stateGroup = new StateGroup();
        Element stateGroupNode = behaviorNode.element(MDLStructure.STATE_GROUP.getInfo());
        // 获取state
        List<Element> states = stateGroupNode.elements(MDLStructure.STATE.getInfo());
        List<State> stateList = new ArrayList<>();
        for (Element stateNode : states) {
            State state = new State();
            setElementAttributes(stateNode.attributes(), state);
            List<Element> events = stateNode.elements(MDLStructure.EVENT.getInfo());
            List<Event> eventList = new ArrayList<>();
            // 获取event
            for (Element eventNode : events) {
                Event event = new Event();
                setElementAttributes(eventNode.attributes(), event);
                Element inputParameterNode = eventNode.element(MDLStructure.INPUT_PARAMETER.getInfo());
                if (inputParameterNode != null){
                    InputParameter inputParameter = new InputParameter();
                    setElementAttributes(inputParameterNode.attributes(), inputParameter);
                    event.setInputParameter(inputParameter);
                }
                Element outputParameterNode = eventNode.element(MDLStructure.OUTPUT_PARAMETER.getInfo());
                if (outputParameterNode != null){
                    OutputParameter outputParameter = new OutputParameter();
                    setElementAttributes(outputParameterNode.attributes(), outputParameter);
                    event.setOutputParameter(outputParameter);
                }
                eventList.add(event);
            }
            state.setEvents(eventList);
            stateList.add(state);
        }

        stateGroup.setStates(stateList);
        behavior.setStateGroup(stateGroup);
        modelClass.setBehavior(behavior);

        return modelClass;
    }

    @Override
    public void setElementAttributes(List<Attribute> attributes, Object obj) throws Exception {
        for (Attribute attribute : attributes) {
            ReflectUtils.setValueByProp(obj,attribute.getName(),attribute.getValue());
        }
    }

    @Override
    public String getParameter(ModelService modelService, String state, String event, String serviceDir, String insDir) {

        ModelClass modelClass = modelService.getModelClass();

        String parameter = null;

        List<State> states = modelClass.getBehavior().getStateGroup().getStates();

        for (State sta : states) {
            if (state.equals(sta.getName())){
                List<Event> events = sta.getEvents();
                for (Event eve : events) {
                    if (event.equals(eve.getName())){
                        if (eve.getInputParameter() != null){
                            parameter = getValueByParamProp(eve.getInputParameter(), modelService, serviceDir, insDir);
                        } else if (eve.getOutputParameter() != null){
                            parameter = getValueByParamProp(eve.getOutputParameter(), modelService, serviceDir, insDir);
                        }
                        return parameter;
                    }

                }
            }
        }

        return parameter;
    }

    // 根据Parameter的属性获取输入输出的值
    private String getValueByParamProp(Parameter parameter, ModelService ms, String serviceDir, String insDir){
        String param = null;
        String dataMIME = parameter.getDataMIME();
        DataMIME mime = DataMIME.getDataMIMEByValue(dataMIME);
        if (mime == null){
            return null;
        }
        switch (mime){
            case TEXT:{
                // param = parameter.getTextValue();
                param = parameter.getValue();
                break;
            }
            case FILE:{
                // TODO: 2022/11/14 还没做这个
                // 如果是file的话要根据url地址下载数据到本地然后返回文件路径
                // param = parameter.getFilePath();
                // param = parameter.getValue();
                // param = workspace + relativeDir + parameter.getValue();
                // String[] split = value.split("/");
                // param = split[split.length - 1];

                // 该工作空间所在的容器创建的模型服务都在 container.repository 目录的 workspace/{containerId}/service 下
                // String hostDir = "/workspace/" + ms.getContainerId() + "/service";
                // String hostDir = ContainerConstants.serviceDir(ms.getContainerId());
                String hostDir = repository + serviceDir + insDir;

                String value = parameter.getValue();
                String url = labDriveUrl + "/file/download/" + value;
                // destDir是宿主机目录
                String filename = downloadFile(url, hostDir);

                param = ContainerConstants.INNER_SERVICE_DIR + insDir + "/" + filename;
                break;
            }
            case UNKNOWN:{
                param = null;
                break;
            }
            default:{

                break;
            }
        }

        return param;
    }


    /**
     * 根据url地址下载数据到本地然后返回文件路径
     *
     * @param url 下载链接
     * @param destDir 目标文件夹
     * @return {@link String} 下载的文件名
     * @author 7bin
     **/
    private String downloadFile(String url, String destDir){
        // String url = labDriveUrl + "/file/download/" + 8286661556551323648L;
        String[] split = url.split("/");
        Long fileId = Long.valueOf(split[split.length - 1]);
        ApiResponse fileInfo = driveClient.getFileInfo(fileId);
        if (!ApiResponse.reqSuccess(fileInfo)){
            throw new ServiceException("指定文件下载失败, [url: " + url + "]");
            // return null;
        }
        HashMap<String, Object> responseData = (HashMap<String, Object>) ApiResponse.getResponseData(fileInfo);
        String suffix = (String)responseData.get("suffix");
        String filename = "gd_" + UUID.fastUUID() + "." + suffix;
        HttpUtil.downloadFile(url, new File(destDir + "/" + filename));
        return filename;
    }
}
