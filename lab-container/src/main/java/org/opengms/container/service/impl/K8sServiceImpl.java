package org.opengms.container.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.opengms.common.TerminalRes;
import org.opengms.container.entity.po.docker.ContainerInfo;
import org.opengms.container.entity.po.docker.ImageInfo;
import org.opengms.container.service.IAbstractContainerService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 直接与k8s交互的操作 实现类
 *
 * @author 7bin
 * @date 2023/04/26
 */
@Slf4j
@Service
public class K8sServiceImpl implements IAbstractContainerService {
    @Override
    public ContainerInfo createContainer(ContainerInfo containerInfo) {
        System.out.println(123);
        return null;
    }

    @Override
    public int updateContainer(ContainerInfo containerInfo) {
        return 0;
    }

    @Override
    public String getContainerStatusByContainerInsId(String containerInsId) {
        return null;
    }

    @Override
    public ImageInfo inspectImage(String sha256) {
        return null;
    }

    @Override
    public List<ImageInfo> listImages() {
        return null;
    }

    @Override
    public List<ContainerInfo> listContainers() {
        return null;
    }

    @Override
    public ContainerInfo selectContainerByInsId(String insId) {
        return null;
    }

    @Override
    public void execCommand() {
    }

    @Override
    public String commitContainer(String containerInsId, String imageName, String tag) {
        return null;
    }

    @Override
    public void saveContainer() {

    }

    @Override
    public TerminalRes exportContainer(String container, String outputPath) {
        return null;
    }

    @Override
    public TerminalRes importContainer(String inputPath, String imageName) {
        return null;
    }

    @Override
    public void startContainer(String containerInsId) {

    }

    @Override
    public void stopContainer(String containerInsId) {

    }

    @Override
    public void removeContainer(String containerInsId) {

    }

    @Override
    public boolean isContainerRunning(String containerInsId) {
        return false;
    }
}
