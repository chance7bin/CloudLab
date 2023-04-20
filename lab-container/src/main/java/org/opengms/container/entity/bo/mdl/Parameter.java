package org.opengms.container.entity.bo.mdl;

import lombok.Data;
import org.opengms.container.entity.SerializableEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author 7bin
 * @date 2022/11/07
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Parameter extends SerializableEntity {

    @XmlAttribute(name = "id")
    String id;

    /** text 文本类型
     *  file 文件类型
     *  unknown 未知类型*/
    @XmlAttribute(name = "dataMIME")
    String dataMIME;

    @XmlAttribute(name = "name")
    String name;

    @XmlAttribute(name = "description")
    String description;

    /** 文本类型：值
     *  文件类型：fileId */
    @XmlAttribute(name = "value")
    String value;

    /** dataMIME是文本类型的话数据存放在该字段 */
    // String textValue;

    /** dataMIME是文件类型的话数据存放在该字段 */
    // String filePath;

}
