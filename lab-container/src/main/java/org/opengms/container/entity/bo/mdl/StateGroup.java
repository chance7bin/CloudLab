package org.opengms.container.entity.bo.mdl;

import lombok.Data;
import org.opengms.container.entity.SerializableEntity;
import org.opengms.container.enums.MDLStructure;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * @author 7bin
 * @date 2022/11/04
 */
@Data
@XmlRootElement(name = "StateGroup")
@XmlAccessorType(XmlAccessType.FIELD)
public class StateGroup extends SerializableEntity {

    @XmlElement(name = "State")
    List<State> states;

}
