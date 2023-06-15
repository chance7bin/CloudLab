package org.opengms.admin.config;

import org.opengms.common.utils.file.FileUploadUtils;
import org.opengms.common.utils.file.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.context.request.RequestContextListener;

/**
 * @author bin
 * @date 2022/08/23
 */
@Configuration
@EnableFeignClients(basePackages = "org.opengms.admin.clients")
// 表示通过aop框架暴露该代理对象,AopContext能够访问
@EnableAspectJAutoProxy(exposeProxy = true)
// 扫描common模块的bean
@ComponentScan(value = {"org.opengms.common"})
public class ApplicationConfig implements ApplicationListener<ApplicationStartedEvent> {

    @Value("${lab.profile}")
    String uploadPath;

    /**
     * 启动项目时需要做的操作
     */
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {

        // 设置文件上传的默认路径
        FileUploadUtils.setDefaultBaseDir(uploadPath);
    }

    // @Bean
    // public RequestContextListener requestContextListener(){
    //     return new RequestContextListener();
    // }

}
