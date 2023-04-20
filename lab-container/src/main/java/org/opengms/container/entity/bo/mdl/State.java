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
@XmlRootElement(name = "State")
@XmlAccessorType(XmlAccessType.FIELD)
public class State extends SerializableEntity {

    @XmlAttribute(name = "id")
    String id;

    @XmlAttribute(name = "name")
    String name;

    @XmlAttribute(name = "description")
    String description;

    @XmlElement(name = "Event")
    List<Event> events;

}
