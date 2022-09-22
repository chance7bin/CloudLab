package org.opengms.admin.service.impl;

import org.opengms.admin.constant.UserConstants;
import org.opengms.admin.entity.po.system.SysUser;
import org.opengms.admin.mapper.SysUserMapper;
import org.opengms.admin.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户业务层实现
 *
 * @author bin
 * @date 2022/08/22
 */
// @Slf4j
@Service
public class SysUserServiceImpl implements ISysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;


    @Override
    public SysUser selectUserById(Long userId) {
        return sysUserMapper.selectUserById(userId);
    }

    @Override
    public SysUser selectUserByUserName(String userName) {
        return sysUserMapper.selectUserByUserName(userName);
    }

    @Override
    public int updateUserProfile(SysUser user) {

        return sysUserMapper.updateUser(user);

    }

    @Override
    public String checkUserNameUnique(String userName) {

        int count = sysUserMapper.checkUserNameUnique(userName);
        if (count > 0)
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;

    }

    @Override
    public boolean registerUser(SysUser user) {

        return sysUserMapper.insertUser(user) > 0;

    }


}
