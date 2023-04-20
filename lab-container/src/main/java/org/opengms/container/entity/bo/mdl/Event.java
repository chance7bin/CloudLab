package org.opengms.container.entity.bo.mdl;

import lombok.Data;
import org.opengms.container.entity.SerializableEntity;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * @author 7bin
 * @date 2022/11/04
 */
@Data
@XmlRootElement(name = "Event")
@XmlAccessorType(XmlAccessType.FIELD)
public class Event extends SerializableEntity {

    @XmlAttribute(name = "id")
    String id;

    @XmlAttribute(name = "name")
    String name;

    /** true 可交互;
     * false 不可交互
     * 输入数据用户需要填入数据, 所以是可交互的)
     * 输出数据不需要用户操作, 所以是不可交互的 */
    @XmlAttribute(name = "interaction")
    Boolean interaction;

    /** true必填; false不必填 */
    @XmlAttribute(name = "required")
    Boolean required;

    @XmlAttribute(name = "description")
    String description;

    @XmlElement(name = "InputParameter")
    InputParameter inputParameter;
    // List<InputParameter> inputParameter;

    @XmlElement(name = "OutputParameter")
    OutputParameter outputParameter;
    // List<OutputParameter> outputParameter;

}
