package org.opengms.admin.service.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import com.sun.org.apache.bcel.internal.generic.NEW;
import lombok.extern.slf4j.Slf4j;
import org.opengms.admin.entity.dto.docker.ContainerInfoDTO;
import org.opengms.admin.entity.dto.docker.ImageInfoDTO;
import org.opengms.admin.entity.dto.docker.JupyterInfoDTO;
import org.opengms.admin.entity.po.docker.ContainerInfo;
import org.opengms.admin.entity.po.docker.JupyterContainer;
import org.opengms.admin.mapper.DockerOperMapper;
import org.opengms.admin.service.IDockerService;
import org.opengms.common.utils.DateUtils;
import org.opengms.common.utils.file.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * docker业务层实现类
 *
 * @author bin
 * @date 2022/10/5
 */
@Slf4j
@Service
public class DockerServiceImpl implements IDockerService {

    @Autowired
    @Qualifier(value = "dockerClient")
    DockerClient dockerClient;

    @Autowired
    DockerOperMapper dockerOperMapper;

    @Value("${socket.port}")
    private int socketPort;

    @Override
    public ContainerInfo createContainer(ContainerInfo containerInfo) {

        // DockerClient client = connect();

        CreateContainerResponse response = initContainer(dockerClient, containerInfo);
        String containerInsId = response.getId();
        //启动
        dockerClient.startContainerCmd(containerInsId).exec();

        containerInfo.setContainerInsId(containerInsId);

        return containerInfo;
    }


    @Override
    public int updateContainer(ContainerInfo containerInfo) {
        return 0;
    }

    @Override
    public List<ImageInfoDTO> listImages() {

        List<Image> images = dockerClient.listImagesCmd().exec();

        List<ImageInfoDTO> imageInfoDTOS = new ArrayList<>();
        for (Image image : images) {
            ImageInfoDTO imageInfoDTO = new ImageInfoDTO();
            imageInfoDTO.setRepoTags(image.getRepoTags()[0]);
            Long size = image.getSize();
            imageInfoDTO.setSize(FileUtils.calcSize(size));
            imageInfoDTOS.add(imageInfoDTO);
        }

        return imageInfoDTOS;

    }

    @Override
    public List<ContainerInfoDTO> listContainers() {

        List<Container> containers = dockerClient.listContainersCmd().exec();

        List<ContainerInfoDTO> containerInfoList = new ArrayList<>();
        for (Container container : containers) {
            ContainerInfoDTO containerInfoDTO = new ContainerInfoDTO();
            JupyterContainer jc = dockerOperMapper.getContainerInfoByInsId(container.getId());
            // TODO: 2022/10/28 获取容器从docker命令行还是数据库？
            if (jc == null){
                continue;
            }
            containerInfoDTO.setContainerId(jc.getContainerId());
            containerInfoDTO.setContainerName(jc.getContainerName());
            containerInfoDTO.setImageName(container.getImage());
            containerInfoDTO.setStatus(container.getState());
            containerInfoDTO.setStarted(container.getStatus());
            // container.getCreated() 的时间戳位数是10位 now.getTime()是13位
            containerInfoDTO.setCreated(DateUtils.getTime2Now(DateUtils.toDate(container.getCreated() * 1000)));
            containerInfoList.add(containerInfoDTO);
        }

        return containerInfoList;

    }

    @Override
    public JupyterInfoDTO getJupyterContainerById(Long id) {

        JupyterContainer jc = dockerOperMapper.getContainerInfoById(id);
        JupyterInfoDTO jupyterInfoDTO = new JupyterInfoDTO();
        if (jc != null){
            BeanUtils.copyProperties(jc, jupyterInfoDTO);
        }
        return jupyterInfoDTO;

    }


    //初始化容器
    private CreateContainerResponse initContainer(DockerClient client, ContainerInfo containerInfo){

        //数据卷 Bind.parse
        List<Bind> binds = new ArrayList<>();
        for (String volume : containerInfo.getVolumeList()) {
            binds.add(Bind.parse(volume));
        }

        //容器启动配置
        HostConfig hostConfig = new HostConfig()
            //端口映射
            .withPortBindings(new Ports(new ExposedPort(containerInfo.getContainerExportPort()), Ports.Binding.bindPort(containerInfo.getHostBindPort())));
            // .withPortBindings(
            //     new PortBinding(Ports.Binding.bindPort(containerInfo.getContainerExportPort()), new ExposedPort(containerInfo.getContainerExportPort())),
            //     new PortBinding(Ports.Binding.bindPort(socketPort), new ExposedPort(socketPort)));

        if (binds.size() != 0){
            //挂载
            hostConfig.withBinds(binds);
        }

        CreateContainerCmd containerCmd = client.createContainerCmd(containerInfo.getImageName())
            //容器名
            .withName(containerInfo.getContainerName())
            //端口映射 内部80端口与外部81端口映射
            // .withHostConfig(new HostConfig().withPortBindings(new Ports(new ExposedPort(80), Ports.Binding.bindPort(81))))
            .withHostConfig(hostConfig);

        //创建
        CreateContainerResponse response = containerCmd.exec();
        log.info("container instance id: " + response.getId());
        return response;

    }




}
