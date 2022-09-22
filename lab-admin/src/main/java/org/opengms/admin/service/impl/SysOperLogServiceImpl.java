package org.opengms.admin.service.impl;

import org.opengms.admin.entity.po.system.SysOperLog;
import org.opengms.admin.mapper.SysOperLogMapper;
import org.opengms.admin.service.ISysOperLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author bin
 * @date 2022/08/23
 */
@Service
public class SysOperLogServiceImpl implements ISysOperLogService {

    @Autowired
    private SysOperLogMapper operLogMapper;

    @Override
    public void insertOperlog(SysOperLog operLog) {
        operLogMapper.insertOperlog(operLog);
    }

    @Override
    public List<SysOperLog> selectAll() {
        return operLogMapper.selectAll();
    }
}
