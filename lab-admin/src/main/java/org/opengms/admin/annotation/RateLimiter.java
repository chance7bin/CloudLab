package org.opengms.admin.annotation;

import org.opengms.admin.constant.CacheConstants;
import org.opengms.admin.enums.LimitType;

import java.lang.annotation.*;

/**
 * 限流注解
 *
 * @author 7bin
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {
    /**
     * 限流key
     */
    public String key() default CacheConstants.RATE_LIMIT_KEY;

    /**
     * 限流时间,单位秒
     */
    public int time() default 60;

    /**
     * 限流次数
     */
    public int count() default 5;

    /**
     * 限流类型
     */
    public LimitType limitType() default LimitType.IP;
}
