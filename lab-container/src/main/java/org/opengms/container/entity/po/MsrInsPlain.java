package org.opengms.container.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.opengms.container.enums.ProcessState;

import java.util.Date;

/**
 * @author 7bin
 * @date 2023/05/17
 */
@Data
public class MsrInsPlain {

    /** 实例id */
    String insId;

    /** 服务名 */
    String serviceName;

    /** 镜像名 */
    String imageName;

    /** 实例当前运行状态 */
    ProcessState currentState;

    /** 实例执行状态 */
    String status;

    /** 任务执行时间 */
    Integer spanTime;

    /** 服务类型 */
    String  serviceType;

    /** 任务开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    Date createTime;


}
