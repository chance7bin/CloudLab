package org.opengms.admin.service.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.opengms.admin.entity.po.docker.ContainerInfo;
import org.opengms.admin.entity.po.docker.JupyterContainer;
import org.opengms.admin.mapper.DockerOperMapper;
import org.opengms.admin.service.IDockerService;
import org.opengms.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * docker业务层实现类
 *
 * @author bin
 * @date 2022/10/5
 */
@Service
public class DockerServiceImpl implements IDockerService {

    @Autowired
    DockerOperMapper dockerOperMapper;


    @Override
    public String createContainer(ContainerInfo containerInfo) {

        DockerClient client = connect();
        //创建
        CreateContainerResponse response = initContainer(client, containerInfo);
        String containerId = response.getId();
        //启动
        client.startContainerCmd(containerId).exec();

        return containerId;
    }

    @Override
    public int insertContainer(ContainerInfo containerInfo) {
        return 0;
    }

    @Override
    public int updateContainer(ContainerInfo containerInfo) {
        return 0;
    }


    //连接docker
    private DockerClient connect() {
        // String host = "tcp://localhost:2333";
        // String apiVersion = "1.38";
        //创建DefaultDockerClientConfig
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            // .withApiVersion(apiVersion)
            // .withDockerHost(host)
            .build();
        //创建DockerHttpClient
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .maxConnections(100)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build();
        //创建DockerClient
        DockerClient client = DockerClientImpl.getInstance(config, httpClient);
        return client;
    }


    //初始化容器
    private CreateContainerResponse initContainer(DockerClient client, ContainerInfo containerInfo){

        //数据卷 Bind.parse
        List<Bind> binds = new ArrayList<>();
        // for (String volume : containerInfo.getVolumeList()) {
        //     binds.add(Bind.parse(volume));
        // }

        //jupyter容器数据卷挂载
        if (containerInfo instanceof JupyterContainer){
            String workspaceVolume = ((JupyterContainer) containerInfo).getWorkspaceVolume();
            String configVolume = ((JupyterContainer) containerInfo).getConfigVolume();
            if (StringUtils.isNotEmpty(workspaceVolume)){
                binds.add(Bind.parse(workspaceVolume));
            }
            if (StringUtils.isNotEmpty(configVolume)){
                binds.add(Bind.parse(configVolume));
            }
        }

        //容器启动配置
        HostConfig hostConfig = new HostConfig()
            //端口映射
            .withPortBindings(new Ports(new ExposedPort(containerInfo.getContainerExportPort()), Ports.Binding.bindPort(containerInfo.getHostBindPort())))
            //挂载
            .withBinds(binds);

        CreateContainerCmd containerCmd = client.createContainerCmd(containerInfo.getImageName())
            //容器名
            .withName(containerInfo.getContainerName())
            //端口映射 内部80端口与外部81端口映射
            // .withHostConfig(new HostConfig().withPortBindings(new Ports(new ExposedPort(80), Ports.Binding.bindPort(81))))
            .withHostConfig(hostConfig);

        //创建
        CreateContainerResponse response = containerCmd.exec();
        System.out.println(response.getId());
        return response;

    }




}
