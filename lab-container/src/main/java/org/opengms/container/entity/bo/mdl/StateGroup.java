package org.opengms.container.entity.bo.mdl;

import lombok.Data;
import org.opengms.container.entity.SerializableEntity;
import org.opengms.container.enums.MDLStructure;

import java.util.List;

/**
 * @author 7bin
 * @date 2022/11/04
 */
@Data
public class StateGroup extends SerializableEntity {

    List<State> states;

}
