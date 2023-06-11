package org.opengms.admin.service;

import org.opengms.admin.entity.po.system.SysRole;

import java.util.List;
import java.util.Set;

/**
 * 角色业务层
 *
 * @author 7bin
 * @date 2023/06/09
 */
public interface ISysRoleService {

    /**
     * 根据条件分页查询角色数据
     *
     * @param role 角色信息
     * @return 角色数据集合信息
     */
    List<SysRole> selectRoleList(SysRole role);

    /**
     * 根据用户ID查询角色权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
     Set<String> selectRolePermissionByUserId(Long userId);


    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    List<SysRole> selectRoleAll();

}
