package org.opengms.admin.service.framework;

import org.opengms.admin.constant.CacheConstants;
import org.opengms.admin.entity.po.system.SysUser;
import org.opengms.admin.exception.ServiceException;
import org.opengms.admin.security.context.AuthenticationContextHolder;
import org.opengms.admin.service.IAsyncService;
import org.opengms.admin.utils.SecurityUtils;
import org.opengms.common.constant.Constants;
import org.opengms.admin.utils.redis.RedisCache;
import org.opengms.common.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 登录密码方法
 * 
 * @author ruoyi
 */
@Component
public class SysPasswordService
{
    @Autowired
    private RedisCache redisCache;

    @Value(value = "${user.password.maxRetryCount}")
    private int maxRetryCount;

    @Value(value = "${user.password.lockTime}")
    private int lockTime;

    @Autowired
    IAsyncService asyncService;

    /**
     * 登录账户密码错误次数缓存键名
     * 
     * @param username 用户名
     * @return 缓存键key
     */
    private String getCacheKey(String username)
    {
        return CacheConstants.PWD_ERR_CNT_KEY + username;
    }

    public void validate(SysUser user)
    {
        Authentication usernamePasswordAuthenticationToken = AuthenticationContextHolder.getContext();
        String username = usernamePasswordAuthenticationToken.getName();
        String password = usernamePasswordAuthenticationToken.getCredentials().toString();

        Integer retryCount = redisCache.get(getCacheKey(username));

        if (retryCount == null)
        {
            retryCount = 0;
        }

        if (retryCount >= Integer.valueOf(maxRetryCount).intValue())
        {
            String message = MessageUtils.message("user.password.retry.limit.exceed", maxRetryCount, lockTime);

            asyncService.recordLogininfor(username, Constants.LOGIN_FAIL,message);

            throw new ServiceException(message);
        }

        if (!matches(user, password))
        {
            String message = MessageUtils.message("user.password.retry.limit.count", retryCount);

            retryCount = retryCount + 1;
            asyncService.recordLogininfor(username, Constants.LOGIN_FAIL, message);
            redisCache.set(getCacheKey(username), retryCount, lockTime, TimeUnit.MINUTES);
            throw new ServiceException(message);
        }
        else
        {
            clearLoginRecordCache(username);
        }
    }

    public boolean matches(SysUser user, String rawPassword)
    {
        return SecurityUtils.matchesPassword(rawPassword, user.getPassword());
    }

    public void clearLoginRecordCache(String loginName)
    {
        if (redisCache.hasKey(getCacheKey(loginName)))
        {
            redisCache.del(getCacheKey(loginName));
        }
    }
}
