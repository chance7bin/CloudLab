package org.opengms.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.opengms.admin.entity.po.system.SysRole;

import java.util.List;

/**
 * 角色表 数据层
 *
 * @author 7bin
 * @date 2023/06/09
 */
@Mapper
public interface SysRoleMapper {
    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> selectRolePermissionByUserId(Long userId);

    /**
     * 根据条件分页查询角色数据
     *
     * @param role 角色信息
     * @return 角色数据集合信息
     */
    List<SysRole> selectRoleList(SysRole role);
}
