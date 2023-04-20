package org.opengms.container.entity.po;

import lombok.Data;

/**
 * 容器汇总表 使用id跟type与每个类别的container关联起来
 *
 * @author 7bin
 * @date 2023/04/11
 */
@Data
public class ContainerRelation {
    /** 容器id */
    Long containerId;

    /** 容器类型 */
    Integer typeCode;

    /** 容器运行的主机ip */
    String hostIP;

    /** 容器运行的主机mac地址 */
    String hostMAC;

    /** 容器运行的主机绑定端口号 */
    Integer port;
}
