package org.opengms.admin.service;

import org.opengms.admin.entity.po.system.SysOperLog;

/**
 * 异步工厂
 *
 * @author bin
 * @date 2022/08/23
 */
public interface IAsyncService {

    void recordOper(SysOperLog operLog);

    void recordLogininfor(String username, String status, String message, Object... args);

    void startSocketListener();

    void startSocketListenerNIO();
}
