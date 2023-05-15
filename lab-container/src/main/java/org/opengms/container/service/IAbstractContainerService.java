package org.opengms.container.service;

import org.opengms.common.TerminalRes;
import org.opengms.container.entity.po.docker.ContainerInfo;
import org.opengms.container.entity.po.docker.ImageInfo;

import java.util.List;

/**
 * 与容器的各种交互的抽象，实现类可以是docker也可以是k8s
 *
 * @author 7bin
 * @date 2023/04/26
 */
public interface IAbstractContainerService {

    /**
     * 创建容器
     * @param containerInfo 容器信息
     * @return org.opengms.admin.entity.po.docker.ContainerInfo
     * @Author bin
     **/
    ContainerInfo createContainer(ContainerInfo containerInfo);

    int updateContainer(ContainerInfo containerInfo);

    String getContainerStatusByContainerInsId(String containerInsId);


    ImageInfo inspectImage(String sha256);

    //获取docker镜像列表
    List<ImageInfo> listImages();

    //获取docker容器列表
    List<ContainerInfo> listContainers();

    ContainerInfo selectContainerByInsId(String insId);

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
