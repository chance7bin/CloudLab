package org.opengms.container.entity.bo.mdl;

import lombok.Data;
import org.opengms.container.entity.SerializableEntity;
import org.opengms.container.enums.MDLStructure;

import javax.xml.bind.annotation.*;

/**
 * @author 7bin
 * @date 2022/11/04
 */
@Data
@XmlRootElement(name = "AttributeSet")
@XmlAccessorType(XmlAccessType.FIELD)
public class AttributeSet extends SerializableEntity {

    @XmlElement(name = "Description")
    Description description;

}
