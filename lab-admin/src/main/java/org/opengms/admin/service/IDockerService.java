package org.opengms.admin.service;

import org.opengms.admin.entity.po.docker.ContainerInfo;
import org.opengms.admin.entity.po.system.SysUser;

/**
 * 与docker有关的业务操作
 *
 * @author bin
 * @date 2022/10/5
 */
public interface IDockerService {


    String createContainer(ContainerInfo containerInfo);


    int insertContainer(ContainerInfo containerInfo);

    int updateContainer(ContainerInfo containerInfo);

}
