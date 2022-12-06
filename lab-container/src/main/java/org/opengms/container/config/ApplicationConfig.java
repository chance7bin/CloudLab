package org.opengms.container.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * @author 7bin
 * @date 2022/12/05
 */
@Configuration
@EnableFeignClients(basePackages = "org.opengms.container.clients")
public class ApplicationConfig {
}
