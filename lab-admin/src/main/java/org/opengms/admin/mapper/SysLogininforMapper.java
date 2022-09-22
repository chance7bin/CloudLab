package org.opengms.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.opengms.admin.entity.po.system.SysLogininfor;

/**
 * @author bin
 * @date 2022/09/01
 */
@Mapper
public interface SysLogininforMapper {

    /**
     * 新增系统登录日志
     *
     * @param logininfor 访问日志对象
     */
    void insertLogininfor(SysLogininfor logininfor);

}
