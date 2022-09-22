package org.opengms.admin.security.handle;

import com.alibaba.fastjson2.JSON;
import org.opengms.admin.constant.HttpStatus;
import org.opengms.admin.entity.dto.LoginUser;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.service.IAsyncService;
import org.opengms.admin.service.framework.TokenService;
import org.opengms.common.constant.Constants;
import org.opengms.common.utils.ServletUtils;
import org.opengms.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义退出处理类 返回成功
 * 
 * @author ruoyi
 */
@Configuration
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler
{
    @Autowired
    private TokenService tokenService;


    @Autowired
    private IAsyncService asyncService;


    /**
     * 退出处理
     * 
     * @return
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException
    {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser))
        {
            String userName = loginUser.getUsername();
            // 删除用户缓存记录
            tokenService.delLoginUser(loginUser.getToken());
            // 记录用户退出日志
            asyncService.recordLogininfor(userName, Constants.LOGOUT, "退出成功");
        }
        ServletUtils.renderString(response, JSON.toJSONString(ApiResponse.error(HttpStatus.SUCCESS, "退出成功")));
    }
}
