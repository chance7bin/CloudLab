package org.opengms.container.service.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import lombok.extern.slf4j.Slf4j;
import org.opengms.common.TerminalRes;
import org.opengms.common.utils.DateUtils;
import org.opengms.common.utils.StringUtils;
import org.opengms.common.utils.TerminalUtils;
import org.opengms.common.utils.file.FileUtils;
import org.opengms.container.entity.dto.docker.ContainerInfoDTO;
import org.opengms.container.entity.dto.docker.ImageInfoDTO;
import org.opengms.container.entity.dto.docker.JupyterInfoDTO;
import org.opengms.container.entity.po.docker.ContainerInfo;
import org.opengms.container.entity.po.docker.JupyterContainer;
import org.opengms.container.mapper.DockerOperMapper;
import org.opengms.container.service.IDockerService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
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

    @Value(value = "${container.repository}")
    private String repository;

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
            JupyterContainer jc = dockerOperMapper.getJupyterContainerInfoByInsId(container.getId());
            // TODO: 2022/10/28 获取容器从docker命令行还是数据库？
            if (jc == null){
                continue;
            }
            containerInfoDTO.setContainerId(jc.getContainerId());
            containerInfoDTO.setContainerName(jc.getContainerName());
            containerInfoDTO.setImageName(container.getImage());
            containerInfoDTO.setStatus(container.getState()); //running
            containerInfoDTO.setStarted(container.getStatus()); //Up 3 hours
            // container.getCreated() 的时间戳位数是10位 now.getTime()是13位
            containerInfoDTO.setCreated(DateUtils.getTime2Now(DateUtils.toDate(container.getCreated() * 1000)));
            containerInfoList.add(containerInfoDTO);
        }

        return containerInfoList;

    }


    @Override
    public void execCommand() {

    }

    @Override
    public void commitContainer() {
        // CommitCmd commitCmd = dockerClient.commitCmd("413a283cfa2a2e0cfcd2a70a77d63f9c524d9f59f65f9f1ca682fd7423c4e1d6");
        // commitCmd.withRepository("java-generate");
        // commitCmd.withTag("1.0");
        // String exec = commitCmd.exec();
        String[] cmd = new String[] {"docker", "commit", "413a283cfa2a2e0cfcd2a70a77d63fca682fd7423c4e", "java-commit:2.0"};
        TerminalRes exec = TerminalUtils.exec(cmd);
        System.out.println(exec);

    }

    @Override
    public void saveContainer() {
        SaveImageCmd saveImageCmd = dockerClient.saveImageCmd("java-generate:1.0");

        InputStream exec = saveImageCmd.exec();
    }

    @Override
    public TerminalRes exportContainer(String container, String outputPath) {
        String[] cmd = new String[] {"docker", "export", "-o", outputPath, container};
        return TerminalUtils.exec(cmd);
        // System.out.println(exec);
    }

    @Override
    public TerminalRes importContainer(String inputPath, String imageName) {
        String[] cmd = new String[] {"docker", "import", inputPath, imageName};
        return TerminalUtils.exec(cmd);
        // System.out.println(exec);
    }

    @Override
    public void startContainer() {
    }

    @Override
    public void stopContainer() {

    }

    @Override
    public void removeContainer(String containerInsId) {
        RemoveContainerCmd removeContainerCmd = dockerClient.removeContainerCmd(containerInsId);
        removeContainerCmd.withForce(true);
        removeContainerCmd.exec();
    }


    //初始化容器
    private CreateContainerResponse initContainer(DockerClient client, ContainerInfo containerInfo){

        //数据卷 Bind.parse
        List<Bind> binds = new ArrayList<>();
        for (String volume : containerInfo.getVolumeList()) {
            volume = formatPathSupportDocker(repository + volume);
            binds.add(Bind.parse(volume));
        }

        //容器启动配置
        HostConfig hostConfig = new HostConfig();

        if (containerInfo.getContainerExportPort() != null && containerInfo.getHostBindPort() != null){
            hostConfig
                //端口映射
                .withPortBindings(new Ports(
                    new ExposedPort(containerInfo.getContainerExportPort()),
                    Ports.Binding.bindPort(containerInfo.getHostBindPort())));

        }

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

        // 添加启动命令
        if (!StringUtils.isEmpty(containerInfo.getCmd())){
            //启动命令
            containerCmd.withCmd(containerInfo.getCmd());
        }

        //创建
        CreateContainerResponse response = containerCmd.exec();
        log.info("container instance id: " + response.getId());
        return response;

    }


    /**
     * 将数据卷目录修改成适合docker的格式 [ /e/... ]
     * @param path
     * @return java.lang.String
     * @Author bin
     **/
    private String formatPathSupportDocker(String path){
        // E:\opengms-lab\container\workspace\test:/opt/notebooks
        // String path = "E:\\opengms-lab\\container\\workspace\\test:/opt/notebooks";
        path = path.replaceAll("\\\\","/");
        int index = path.indexOf(":");
        String outputPath = path;
        if (index == 1){
            // 只有 E: 这种形式的才要进行处理
            outputPath = "/" + Character.toString(path.charAt(0)).toLowerCase() + path.substring(index + 1);
        }
        return outputPath;
    }

}
