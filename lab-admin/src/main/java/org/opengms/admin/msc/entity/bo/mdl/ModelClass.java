package org.opengms.admin.msc.entity.bo.mdl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengms.admin.entity.SerializableEntity;

/**
 * @author 7bin
 * @date 2022/11/04
 */
@Data
// @AllArgsConstructor
// @NoArgsConstructor
public class ModelClass {

    String id;

    String name;

    AttributeSet attributeSet;

    Behavior behavior;

}
