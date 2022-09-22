package org.opengms.admin.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author bin
 * @date 2022/08/23
 */
@Configuration
// 表示通过aop框架暴露该代理对象,AopContext能够访问
@EnableAspectJAutoProxy(exposeProxy = true)
// 扫描common模块的bean
@ComponentScan(value = {"org.opengms.common"})
public class ApplicationConfig {

}
