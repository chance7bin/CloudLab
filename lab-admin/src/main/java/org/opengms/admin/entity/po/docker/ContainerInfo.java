package org.opengms.admin.entity.po.docker;

import cn.hutool.core.lang.Snowflake;
import lombok.Data;
import org.opengms.admin.entity.BaseEntity;
import org.opengms.common.utils.uuid.SnowFlake;

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
    Long containerId = SnowFlake.nextId();

    /** 容器实例id */
    String containerInsId;

    /** 容器名 (由镜像名以及容器id组成) */
    String containerName;

    /** 镜像名 */
    String imageName;

    /** 容器对外暴露端口 */
    Integer containerExportPort;

    /** 容器绑定端口 */
    Integer hostBindPort;

    /** 挂载数据卷列表 */
    List<String> volumeList = new ArrayList<>();


}
