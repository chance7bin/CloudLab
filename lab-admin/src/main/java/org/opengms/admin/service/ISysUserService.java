package org.opengms.admin.service;

import org.opengms.admin.entity.po.system.SysUser;

/**
 * @Description 用户业务层
 * @author bin
 * @date 2022/08/22
 */
public interface ISysUserService {
    SysUser selectUserById(Long userId);

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    SysUser selectUserByUserName(String userName);

    /**
     * 修改用户基本信息
     *
     * @param user 用户信息
     * @return 结果
     */
    int updateUserProfile(SysUser user);

    /**
     * 校验用户名称是否唯一
     *
     * @param userName 用户名称
     * @return 结果
     */
    String checkUserNameUnique(String userName);


    /**
     * 注册用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    boolean registerUser(SysUser user);


    boolean createJupyterContainer(String userName);

}
