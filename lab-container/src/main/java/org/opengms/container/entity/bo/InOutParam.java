package org.opengms.container.entity.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型输入输出类
 *
 * @author 7bin
 * @date 2022/12/06
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class InOutParam {

    String state;

    String event;

    String parameter;

    String stateId;

    String eventId;

    String parameterId;

    /** 文件服务器的文件id */
    String driveFileId;

    /** text 文本类型
     *  file 文件类型
     *  unknown 未知类型*/
    String dataMIME;

    /** 文本类型：值
     *  文件类型：容器内部路径 */
    String value;

    Boolean destroyed;

    Boolean required;

    public InOutParam(String state, String event, String dataMIME, String value) {
        this.state = state;
        this.event = event;
        this.dataMIME = dataMIME;
        this.value = value;
    }
}
