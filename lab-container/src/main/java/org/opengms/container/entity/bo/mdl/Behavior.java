package org.opengms.container.entity.bo.mdl;

import lombok.Data;
import org.opengms.container.entity.SerializableEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author 7bin
 * @date 2022/11/04
 */
@Data
@XmlRootElement(name = "Behavior")
@XmlAccessorType(XmlAccessType.FIELD)
public class Behavior extends SerializableEntity {

    @XmlElement(name = "StateGroup")
    StateGroup stateGroup;

}
