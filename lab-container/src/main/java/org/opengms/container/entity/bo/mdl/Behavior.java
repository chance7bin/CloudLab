package org.opengms.container.entity.bo.mdl;

import lombok.Data;
import org.opengms.container.entity.SerializableEntity;

/**
 * @author 7bin
 * @date 2022/11/04
 */
@Data
public class Behavior extends SerializableEntity {

    StateGroup stateGroup;

}
