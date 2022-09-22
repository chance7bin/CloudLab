package org.opengms.admin.service.impl;

import org.opengms.admin.entity.po.system.SysLogininfor;
import org.opengms.admin.mapper.SysLogininforMapper;
import org.opengms.admin.service.ISysLogininforService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bin
 * @date 2022/08/25
 */
@Service
public class SysLogininforServiceImpl implements ISysLogininforService {

    @Autowired
    private SysLogininforMapper logininforMapper;


    @Override
    public void insertLogininfor(SysLogininfor logininfor) {
        logininforMapper.insertLogininfor(logininfor);
    }
}
