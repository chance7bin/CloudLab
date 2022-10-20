package org.opengms.admin.service;

import org.opengms.admin.entity.dto.docker.ContainerInfoDTO;
import org.opengms.admin.entity.dto.docker.ImageInfoDTO;
import org.opengms.admin.entity.dto.docker.JupyterInfoDTO;
import org.opengms.admin.entity.po.docker.ContainerInfo;

import java.util.List;

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


    //获取docker镜像列表
    List<ImageInfoDTO> listImages();

    //获取docker容器列表
    List<ContainerInfoDTO> listContainers();


    JupyterInfoDTO getJupyterContainerById(Long id);
}
