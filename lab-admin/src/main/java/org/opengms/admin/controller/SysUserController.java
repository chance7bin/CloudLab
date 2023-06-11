package org.opengms.admin.controller;

import org.apache.commons.lang3.ArrayUtils;
import org.opengms.admin.annotation.Log;
import org.opengms.admin.controller.common.BaseController;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.dto.TableDataInfo;
import org.opengms.admin.entity.po.system.SysRole;
import org.opengms.admin.entity.po.system.SysUser;
import org.opengms.admin.enums.BusinessType;
import org.opengms.admin.service.ISysRoleService;
import org.opengms.admin.service.ISysUserService;
import org.opengms.admin.utils.SecurityUtils;
import org.opengms.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description 用户接口
 * @author bin
 * @date 2022/08/22
 */
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController {

    @Autowired
    ISysUserService userService;

    @Autowired
    ISysRoleService roleService;

    /**
     * 根据用户编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping(value = { "/", "/{userId}" })
    public ApiResponse getInfo(@PathVariable(value = "userId", required = false) Long userId)
    {
        // TODO: 2023/6/10 @DataScope注解的使用
        // userService.checkUserDataScope(userId);
        ApiResponse response = ApiResponse.success();
        List<SysRole> roles = roleService.selectRoleAll();
        response.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        if (StringUtils.isNotNull(userId))
        {
            SysUser sysUser = userService.selectUserById(userId);
            response.put(ApiResponse.DATA_TAG, sysUser);
            response.put("roleIds", sysUser.getRoles().stream().map(SysRole::getRoleId).collect(Collectors.toList()));
        }
        return response;
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    public TableDataInfo list(SysUser user)
    {
        startPage();
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    @GetMapping(value = "/auth" )
    public ApiResponse auth(){
        // Long userId = 101L;
        return ApiResponse.success();
    }

    /**
     * 状态修改
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/status")
    public ApiResponse changeStatus(@RequestBody SysUser user)
    {
        userService.checkUserAllowed(user);
        SysUser sysUser = new SysUser();
        sysUser.setUserId(user.getUserId());
        sysUser.setStatus(user.getStatus());
        user.setUpdateBy(getUsername());
        return affectRows(userService.updateUserStatus(user));
    }

    /**
     * 修改用户
     */
    // @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public ApiResponse edit(@Validated @RequestBody SysUser user)
    {
        userService.checkUserAllowed(user);
        if (!userService.checkUserNameUnique(user))
        {
            return error("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            return error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            return error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setUpdateBy(getUsername());
        return affectRows(userService.updateUser(user));
    }


    /**
     * 删除用户
     */
    // @PreAuthorize("@ss.hasPermi('system:user:remove')")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public ApiResponse remove(@PathVariable Long[] userIds)
    {
        if (ArrayUtils.contains(userIds, getUserId()))
        {
            return error("当前用户不能删除");
        }
        return affectRows(userService.deleteUserByIds(userIds));
    }

    /**
     * 重置密码
     */
    // @PreAuthorize("@ss.hasPermi('system:user:resetPwd')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/reset/pwd")
    public ApiResponse resetPwd(@RequestBody SysUser user)
    {
        userService.checkUserAllowed(user);
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        user.setUpdateBy(getUsername());
        return affectRows(userService.resetPwd(user));
    }

}
