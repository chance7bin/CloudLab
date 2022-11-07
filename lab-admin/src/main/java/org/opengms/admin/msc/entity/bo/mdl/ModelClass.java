package org.opengms.admin.msc.entity.bo.mdl;

import lombok.Data;
import org.opengms.admin.entity.SerializableEntity;

/**
 * @author 7bin
 * @date 2022/11/04
 */
@Data
public class ModelClass extends SerializableEntity {

    String id;

    String name;

    AttributeSet attributeSet;

    Behavior behavior;

}
