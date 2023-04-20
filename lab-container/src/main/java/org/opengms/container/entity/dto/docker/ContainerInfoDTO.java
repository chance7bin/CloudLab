package org.opengms.container.entity.dto.docker;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

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

    /** 容器实例id */
    String containerInsId;

    /** 镜像名 */
    private String imageName;

    /** 容器状态 */
    private String status;

    /** 启动时间 */
    private String started;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date created;

    /** 绑定的主机端口号 */
    private Integer hostBindPort;

    /** 绑定的主机ip */
    private String hostIP;


}
