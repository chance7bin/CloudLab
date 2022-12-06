package org.opengms.container.entity.bo.mdl;

import lombok.Data;
import org.opengms.container.entity.SerializableEntity;

/**
 * @author 7bin
 * @date 2022/11/07
 */
@Data
public class Parameter extends SerializableEntity {

    String id;

    /** text 文本类型
     *  file 文件类型
     *  unknown 未知类型*/
    String dataMIME;

    String name;

    String description;

    /** 文本类型：值
     *  文件类型：fileId */
    String value;

    /** dataMIME是文本类型的话数据存放在该字段 */
    // String textValue;

    /** dataMIME是文件类型的话数据存放在该字段 */
    // String filePath;

}
