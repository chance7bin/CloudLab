package org.opengms.admin.service.impl;

import org.opengms.admin.entity.po.system.SysRole;
import org.opengms.admin.mapper.SysRoleMapper;
import org.opengms.admin.service.ISysRoleService;
import org.opengms.common.utils.StringUtils;
import org.opengms.common.utils.spring.SpringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author 7bin
 * @date 2023/06/09
 */
@Service
public class SysRoleServiceImpl implements ISysRoleService {

    @Autowired
    private SysRoleMapper roleMapper;

    @Override
    // @DataScope(deptAlias = "d")
    public List<SysRole> selectRoleList(SysRole role)
    {
        return roleMapper.selectRoleList(role);
    }

    @Override
    public Set<String> selectRolePermissionByUserId(Long userId) {
        List<SysRole> perms = roleMapper.selectRolePermissionByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (SysRole perm : perms)
        {
            if (StringUtils.isNotNull(perm))
            {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

    @Override
    public List<SysRole> selectRoleAll()
    {
        // TODO: 2023/6/11 AopContext.currentProxy() 获取到的是controller代理类 为什么？？
        // 这里用aop是因为要走@DataScope过滤数据
        // return SpringUtils.getAopProxy(this).selectRoleList(new SysRole());
        return selectRoleList(new SysRole());
    }

}
