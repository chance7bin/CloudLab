package org.opengms.admin.msc.enums;

import org.omg.CORBA.UNKNOWN;

/**
 * 模型执行步骤状态
 *
 * @author 7bin
 * @date 2022/10/21
 */
public enum ProcessStatus {
    FINISH, // 完成
    NOTREADY,// 未完成
    ERROR,// 失败
    UNKNOWN// 未知状态
}
