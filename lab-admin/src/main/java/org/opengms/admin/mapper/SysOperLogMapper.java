package org.opengms.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.opengms.admin.entity.po.system.SysOperLog;

import java.util.List;

/**
 * @author bin
 * @date 2022/08/23
 */
@Mapper
public interface SysOperLogMapper {

    /**
     * 新增操作日志
     *
     * @param operLog 操作日志对象
     */
    void insertOperlog(SysOperLog operLog);

    List<SysOperLog> selectAll();
}
