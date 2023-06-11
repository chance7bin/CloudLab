package org.opengms.admin.service.impl;

import org.opengms.admin.constant.UserConstants;
import org.opengms.admin.entity.dto.LoginUser;
import org.opengms.admin.entity.dto.RegisterDTO;
import org.opengms.admin.entity.po.system.SysUser;
import org.opengms.admin.exception.ServiceException;
import org.opengms.admin.security.context.AuthenticationContextHolder;
import org.opengms.admin.service.IAsyncService;
import org.opengms.admin.service.ISysConfigService;
import org.opengms.admin.service.ISysLoginService;
import org.opengms.admin.service.ISysUserService;
import org.opengms.admin.service.framework.TokenService;
import org.opengms.admin.utils.SecurityUtils;
import org.opengms.common.constant.Constants;
import org.opengms.common.utils.DateUtils;
import org.opengms.common.utils.MessageUtils;
import org.opengms.common.utils.ServletUtils;
import org.opengms.common.utils.StringUtils;
import org.opengms.common.utils.ip.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author bin
 * @date 2022/08/26
 */
@Service
public class SysLoginServiceImpl implements ISysLoginService {

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    IAsyncService asyncService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    TokenService tokenService;

    @Autowired
    ISysConfigService configService;

    @Override
    public String login(String username, String password, String code, String uuid) {

        boolean captchaEnabled = configService.selectCaptchaEnabled();
        // 验证码开关
        if (captchaEnabled)
        {
            // validateCaptcha(username, code, uuid);
        }
        // 用户验证
        Authentication authentication = null;
        try
        {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        }
        catch (Exception e)
        {
            if (e instanceof BadCredentialsException)
            {
                String message = MessageUtils.message("user.password.not.match");
                asyncService.recordLogininfor(username, Constants.LOGIN_FAIL, message);
                throw new ServiceException(message);
            }
            else
            {
                asyncService.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage());
                throw new ServiceException(e.getMessage());
            }
        }
        asyncService.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        recordLoginInfo(loginUser.getUserId());
        // 生成token
        return tokenService.createToken(loginUser);

    }

    @Override
    public String register(RegisterDTO registerBody) {
        String msg = "", username = registerBody.getUsername(), password = registerBody.getPassword();

        boolean captchaEnabled = configService.selectCaptchaEnabled();
        // 验证码开关
        if (captchaEnabled)
        {
            // validateCaptcha(username, registerBody.getCode(), registerBody.getUuid());
        }

        if (StringUtils.isEmpty(username))
        {
            msg = "用户名不能为空";
        }
        else if (StringUtils.isEmpty(password))
        {
            msg = "用户密码不能为空";
        }
        else if (username.length() < UserConstants.USERNAME_MIN_LENGTH
            || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            msg = "账户长度必须在2到20个字符之间";
        }
        else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
            || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            msg = "密码长度必须在5到20个字符之间";
        }
        else if (userService.checkUserNameUnique(username))
        {
            msg = "保存用户'" + username + "'失败，注册账号已存在";
        }
        else
        {
            SysUser sysUser = new SysUser();
            sysUser.setUserName(username);
            sysUser.setPassword(SecurityUtils.encryptPassword(registerBody.getPassword()));
            boolean regFlag = userService.registerUser(sysUser);
            if (!regFlag)
            {
                msg = "注册失败,请联系系统管理人员";
            }
            else
            {
                asyncService.recordLogininfor(username, Constants.REGISTER,
                    MessageUtils.message("user.register.success"));
            }
        }
        return msg;


    }


    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId)
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(IpUtils.getIpAddr(ServletUtils.getRequest()));
        sysUser.setLoginDate(DateUtils.getNowDate());
        userService.updateUserProfile(sysUser);
    }

}
