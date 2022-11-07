package org.opengms.admin.msc.enums;

/**
 * mdl节点结构
 *
 * @author 7bin
 * @date 2022/11/07
 */
public enum MDLStructure {

    MODEL_CLASS("ModelClass"),

    ATTRIBUTE_SET("AttributeSet"),

    DESCRIPTION("Description"),

    BEHAVIOR("Behavior"),

    STATE_GROUP("StateGroup"),

    STATE("State"),

    EVENT("Event"),

    INPUT_PARAMETER("InputParameter"),

    OUTPUT_PARAMETER("OutputParameter"),

    ;

    private final String info;

    MDLStructure(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

}
