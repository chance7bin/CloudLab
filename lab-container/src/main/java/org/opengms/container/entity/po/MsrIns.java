package org.opengms.container.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.opengms.container.entity.BaseEntity;
import org.opengms.container.entity.bo.InOutParam;
import org.opengms.container.entity.bo.Log;
import org.opengms.container.entity.po.ModelService;
import org.opengms.container.enums.ProcessState;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 模型服务运行实例 model service run instance
 *
 * @author 7bin
 * @date 2022/10/21
 */
@Data
public class MsrIns extends BaseEntity {

    /** 客户端channel */
    @JsonIgnore
    SocketChannel socketChannel;

    /** 模型运行实例id  */
    String msriId;

    /** 实例当前运行状态 */
    ProcessState currentState;

    /** 该客户端对应的mdl对象 */
    // ModelClass modelClass;

    /** 该客户端对应的模型服务对象 */
    ModelService modelService;
    Long msId;

    /** 该客户端对应的实例路径 */
    String instanceDir;

    /** 输入数据 */
    List<InOutParam> inputs;

    /** 输出数据 */
    List<InOutParam> outputs;

    /** 日志记录 */
    List<Log> logs = new ArrayList<>();

    // String state;

    // String event;

    /** 实例执行状态 */
    String status;

    /** 任务开始时间 */
    Date startTime;

    /** 任务执行时间 */
    Integer spanTime;

}
