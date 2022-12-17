package org.opengms.container.entity.po.docker;

import lombok.Data;
import org.opengms.container.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * docker容器实例信息
 *
 * @author bin
 * @date 2022/10/5
 */
@Data
public class ContainerInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 容器id */
    Long containerId;

    /** 容器实例id */
    String containerInsId;

    /** 容器名 (由镜像名以及容器id组成) */
    String containerName;

    /** 镜像名 */
    String imageName;

    /** 镜像id */
    Long imageId;

    /** 容器对外暴露端口 */
    Integer containerExportPort;

    /** 容器绑定端口 */
    Integer hostBindPort;

    /** 挂载数据卷列表 宿主机目录中表示的只是相对路径 实际路径需加上${repository} */
    List<String> volumeList = new ArrayList<>();

    /** 启动命令 */
    List<String> cmd;

    /** 容器状态 */
    String status;

    /** 容器是否被删除 */
    Boolean delFlag;

}
