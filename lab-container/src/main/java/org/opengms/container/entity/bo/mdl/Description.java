package org.opengms.container.entity.bo.mdl;

import lombok.Data;
import org.opengms.container.entity.SerializableEntity;

import javax.xml.bind.annotation.*;

/**
 * @author 7bin
 * @date 2022/11/04
 */
@Data
@XmlRootElement(name = "Description")
@XmlAccessorType(XmlAccessType.FIELD)
public class Description extends SerializableEntity {

    @XmlAttribute(name = "name")
    String name;

}
