package org.opengms.drive.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置文件-文件存储路径属性
 *
 * @author wgb
 * @date 2021/10/16 10:42
 */
@Getter
@Setter
@ConfigurationProperties("file")
public class FilePathProperties {
    /**
     * 文件存储路径
     */
    private String savePath;
    /**
     * 断点传输配置文件存储路径
     */
    private String confPath;
}
