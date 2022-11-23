package org.opengms.admin.entity.dto.container.mdl;

import lombok.Data;
import org.opengms.admin.entity.SerializableEntity;

/**
 * @author 7bin
 * @date 2022/11/04
 */
@Data
public class Event extends SerializableEntity {

    String id;

    String name;

    /** true 可交互;
     * false 不可交互
     * 输入数据用户需要填入数据, 所以是可交互的)
     * 输出数据不需要用户操作, 所以是不可交互的 */
    Boolean interaction;

    /** true必填; false不必填 */
    Boolean required;

    String description;

    InputParameter inputParameter;

    OutputParameter outputParameter;

}
