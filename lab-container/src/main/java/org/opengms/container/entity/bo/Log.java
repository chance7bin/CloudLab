package org.opengms.container.entity.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengms.container.enums.ProcessState;
import org.opengms.container.enums.DataFlag;

import java.util.Date;

/**
 * 模型运行日志
 *
 * @author 7bin
 * @date 2022/11/07
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Log {

    ProcessState type;

    String state;

    String event;

    DataFlag dataFlag;

    String message;

    Date createTime;

    public Log(String message) {
        this.message = message;
    }


    public Log(DataFlag dataFlag, String message) {
        this.dataFlag = dataFlag;
        this.message = message;
    }
}
