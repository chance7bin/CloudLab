package org.opengms.container.entity.bo.mdl;

import lombok.Data;

import javax.xml.bind.annotation.*;

/**
 * @author 7bin
 * @date 2022/11/04
 */
@Data
// @AllArgsConstructor
// @NoArgsConstructor
//根元素
@XmlRootElement(name = "ModelClass")
//访问类型，通过字段
@XmlAccessorType(XmlAccessType.FIELD)
// 控制JAXB 绑定类中属性和字段的排序
@XmlType(propOrder = {
    "attributeSet",
    "behavior",
})
public class ModelClass {

    @XmlAttribute(name = "id")
    String id;

    @XmlAttribute(name = "name")
    String name;

    @XmlElement(name = "AttributeSet")
    AttributeSet attributeSet;

    @XmlElement(name = "Behavior")
    Behavior behavior;

}
