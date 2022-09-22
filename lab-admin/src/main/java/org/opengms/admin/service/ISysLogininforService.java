package org.opengms.admin.service;

import org.opengms.admin.entity.po.system.SysLogininfor;

/**
 * 系统访问日志情况信息 服务层
 *
 * @author bin
 * @date 2022/08/25
 */
public interface ISysLogininforService {

    /**
     * 新增系统登录日志
     *
     * @param logininfor 访问日志对象
     */
    void insertLogininfor(SysLogininfor logininfor);


}
