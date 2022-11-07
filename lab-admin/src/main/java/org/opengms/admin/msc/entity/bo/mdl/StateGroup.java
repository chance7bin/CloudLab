package org.opengms.admin.msc.entity.bo.mdl;

import lombok.Data;
import org.opengms.admin.entity.SerializableEntity;
import org.opengms.admin.msc.enums.MDLStructure;

import java.util.List;

/**
 * @author 7bin
 * @date 2022/11/04
 */
@Data
public class StateGroup extends SerializableEntity {

    List<State> states;

}
