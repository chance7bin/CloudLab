package org.opengms.container.entity.bo.docker;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 容器启动相关参数
 *
 * @author bin
 * @date 2022/10/11
 */
@Data
public class LaunchParams {

    /** 镜像名 */
    String imageName;

    /** 容器对外暴露端口 */
    Integer containerExportPort;

    /** 容器绑定端口 */
    Integer hostBindPort;

    /** 挂载数据卷列表 */
    List<String> volumeList = new ArrayList<>();

}
