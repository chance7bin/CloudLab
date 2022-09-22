package org.opengms.admin.service;


import org.opengms.admin.entity.po.system.SysOperLog;

import java.util.List;

/**
 * 操作日志 服务层
 *
 * @author bin
 * @date 2022/08/23
 */
public interface ISysOperLogService {


    /**
     * 新增操作日志
     *
     * @param operLog 操作日志对象
     */
    void insertOperlog(SysOperLog operLog);

    List<SysOperLog> selectAll();
}
