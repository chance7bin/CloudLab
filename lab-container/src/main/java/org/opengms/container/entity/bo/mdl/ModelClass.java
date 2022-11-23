package org.opengms.container.entity.bo.mdl;

import lombok.Data;

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
