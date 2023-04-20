package org.opengms.container.service;

import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.Container;
import org.opengms.common.TerminalRes;
import org.opengms.container.entity.dto.docker.ContainerInfoDTO;
import org.opengms.container.entity.dto.docker.ImageInfoDTO;
import org.opengms.container.entity.dto.docker.JupyterInfoDTO;
import org.opengms.container.entity.po.docker.ContainerInfo;

import java.util.List;

/**
 * 直接与docker交互的操作
 *
 * @author bin
 * @date 2022/10/5
 */
public interface IDockerService {


    /**
     * 创建容器
     * @param containerInfo 容器信息
     * @return org.opengms.admin.entity.po.docker.ContainerInfo
     * @Author bin
     **/
    ContainerInfo createContainer(ContainerInfo containerInfo);

    int updateContainer(ContainerInfo containerInfo);

    String getContainerStatusByContainerInsId(String containerInsId);


    InspectImageResponse inspectImage(String sha256);

    //获取docker镜像列表
    List<ImageInfoDTO> listImages();

    //获取docker容器列表
    List<Container> listContainers();

    Container selectContainerByInsId(String insId);

    void execCommand();

    /**
     * 导出镜像
     *
     * @param containerInsId docker中的容器实例id
     * @param imageName 镜像名
     * @param tag 镜像版本
     * @return {@link String} 镜像的sha256
     * @author 7bin
     **/
    String commitContainer(String containerInsId, String imageName, String tag);

    void saveContainer();

    TerminalRes exportContainer(String container, String outputPath);

    TerminalRes importContainer(String inputPath, String imageName);

    void startContainer(String containerInsId);

    void stopContainer(String containerInsId);

    void removeContainer(String containerInsId);

    boolean isContainerRunning(String containerInsId);
}
