package org.opengms.container.entity.dto.docker;

import lombok.Data;

/**
 * 创建新环境（镜像）传输对象
 *
 * @author 7bin
 * @date 2023/04/18
 */
@Data
public class EnvDTO {

    /** 基于当前容器创建新环境 */
    Long containerId;

    /** 新环境名称 */
    String envName;

    /** 新环境标签 */
    String tag = "1.0";

}
