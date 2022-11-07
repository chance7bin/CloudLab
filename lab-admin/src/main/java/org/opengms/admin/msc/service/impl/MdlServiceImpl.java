package org.opengms.admin.msc.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.opengms.admin.msc.entity.bo.mdl.*;
import org.opengms.admin.msc.enums.DataMIME;
import org.opengms.admin.msc.enums.MDLStructure;
import org.opengms.admin.msc.service.IMdlService;
import org.opengms.common.utils.ReflectUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 7bin
 * @date 2022/11/07
 */
@Service
@Slf4j
public class MdlServiceImpl implements IMdlService {


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
    public String getParameter(ModelClass modelClass, String state, String event) {

        String parameter = null;

        List<State> states = modelClass.getBehavior().getStateGroup().getStates();

        for (State sta : states) {
            if (state.equals(sta.getName())){
                List<Event> events = sta.getEvents();
                for (Event eve : events) {
                    if (event.equals(eve.getName())){
                        if (eve.getInputParameter() != null){
                            parameter = getValueByParamProp(eve.getInputParameter());
                        } else if (eve.getOutputParameter() != null){
                            parameter = getValueByParamProp(eve.getOutputParameter());
                        }
                        return parameter;
                    }

                }
            }
        }

        return parameter;
    }

    // 根据Parameter的属性获取输入输出的值
    private String getValueByParamProp(Parameter parameter){
        String param = null;
        String dataMIME = parameter.getDataMIME();
        DataMIME mime = DataMIME.getDataMIMEByValue(dataMIME);
        if (mime == null){
            return null;
        }
        switch (mime){
            case TEXT:{
                param = parameter.getTextValue();
                break;
            }
            case FILE:{
                param = parameter.getFilePath();
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
}
