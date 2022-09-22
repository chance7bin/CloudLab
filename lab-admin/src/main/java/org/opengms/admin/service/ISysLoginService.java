package org.opengms.admin.service;

import org.opengms.admin.entity.dto.RegisterDTO;

/**
 * 登录注册模块
 *
 * @author bin
 * @date 2022/08/26
 */
public interface ISysLoginService {

    /**
     * 登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    String login(String username, String password, String code, String uuid);


    /**
     * 注册
     */
    String register(RegisterDTO registerBody);

}
