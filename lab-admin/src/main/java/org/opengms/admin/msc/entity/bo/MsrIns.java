package org.opengms.admin.msc.entity.bo;

import lombok.Data;
import org.opengms.admin.msc.entity.bo.mdl.ModelClass;
import org.opengms.admin.msc.entity.po.ModelService;
import org.opengms.admin.msc.enums.ProcessState;

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
public class MsrIns {

    /** 客户端channel */
    SocketChannel socketChannel;

    /** 模型运行实例id  */
    String msriId;

    /** 实例当前运行状态 */
    ProcessState currentState;

    /** 该客户端对应的mdl对象 */
    // ModelClass modelClass;

    /** 该客户端对应的mdl对象 */
    ModelService modelService;

    /** 输入数据 */
    // List<String> inputs;

    /** 输出数据 */
    // List<String> outputs;

    /** 日志记录 */
    List<Log> logs = new ArrayList<>();

    /** 模型开始运行时间 */
    Date createTime;

    String state;

    String event;
}
