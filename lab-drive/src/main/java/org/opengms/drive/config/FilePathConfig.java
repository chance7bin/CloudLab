package org.opengms.drive.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 注入属性类
 *
 * @author 7bin
 * @date
 */
@Configuration
@EnableConfigurationProperties(FilePathProperties.class)
public class FilePathConfig {
}
