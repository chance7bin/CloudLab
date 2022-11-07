package org.opengms.admin.msc.entity.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengms.admin.msc.enums.ProcessState;
import org.opengms.admin.msc.enums.ProcessStatus;

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

    ProcessState Type;

    String state;

    String event;

    ProcessStatus status;

    String message;

    Date createTime;

    public Log(String message) {
        this.message = message;
    }
}
