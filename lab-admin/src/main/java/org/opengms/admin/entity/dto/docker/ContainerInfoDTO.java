package org.opengms.admin.entity.dto.docker;

import lombok.Data;

/**
 * @author bin
 * @date 2022/10/13
 */
@Data
public class ContainerInfoDTO {

    /** 容器id */
    private Long containerId;

    /** 容器名 */
    private String containerName;

    /** 镜像名 */
    private String imageName;

    /** 容器状态 */
    private String status;

    /** 启动时间 */
    private String started;

    /** 绑定的主机端口号 */
    private Integer hostBindPort;


}
