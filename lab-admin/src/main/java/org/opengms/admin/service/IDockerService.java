package org.opengms.admin.service;

import org.opengms.admin.entity.bo.docker.LaunchParams;
import org.opengms.admin.entity.po.docker.ContainerInfo;
import org.opengms.admin.entity.po.system.SysUser;

/**
 * 与docker有关的业务操作
 *
 * @author bin
 * @date 2022/10/5
 */
public interface IDockerService {


    /**
     * 创建容器
     * @param containerInfo
     * @return org.opengms.admin.entity.po.docker.ContainerInfo
     * @Author bin
     **/
    ContainerInfo createContainer(ContainerInfo containerInfo);

    int updateContainer(ContainerInfo containerInfo);

}
