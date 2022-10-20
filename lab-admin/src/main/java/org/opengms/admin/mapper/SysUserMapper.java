package org.opengms.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.opengms.admin.entity.po.system.SysUser;

/**
 * @Description 用户数据层
 * @author bin
 * @date 2022/08/22
 */
@Mapper
public interface SysUserMapper {

    /**
     * 根据userid 返回用户信息
     *
     * @param userId
     * @return org.opengms.admin.entity.po.system.SysUser
     * @author bin
     **/
    SysUser selectUserById(Long userId);

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    SysUser selectUserByUserName(String userName);

    /**
     * 修改用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    int updateUser(SysUser user);

    /**
     * 校验用户名称是否唯一
     *
     * @param userName 用户名称
     * @return 结果
     */
    int checkUserNameUnique(String userName);

    /**
     * 新增用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    int insertUser(SysUser user);
}
