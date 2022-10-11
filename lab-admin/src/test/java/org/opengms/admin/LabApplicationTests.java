package org.opengms.admin;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.junit.jupiter.api.Test;
import org.opengms.admin.config.AppConfig;
import org.opengms.admin.entity.bo.Server;
import org.opengms.admin.entity.po.docker.JupyterContainer;
import org.opengms.admin.mapper.DockerOperMapper;
import org.opengms.admin.service.IWorkspaceService;
import org.opengms.common.utils.file.FileUtils;
import org.opengms.common.utils.ip.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;

/**
 * @author bin
 * @date 2022/09/22
 */
@SpringBootTest
public class LabApplicationTests {

    // 生成jupyter配置文件
    @Test
    void generateJupyterConfig() throws IOException {

        String resourcePath = "static/jupyter_lab_config.py";

        String s = FileUtils.readResourceTxtFile(resourcePath);

        // System.out.println(s);

        s += "c.ServerApp.token = '66666'";

        FileUtils.writeBytes(s.getBytes(), "E:","jupyter_lab_config.py");

    }

    // 得到服务器信息
    @Test
    void getOSInfo() throws Exception {
        Server server = new Server();
        server.copyTo();
        System.out.println(server);

    }

    // 得到mac地址
    @Test
    void getMac(){
        System.out.println(IpUtils.getMacAddress());

    }

    //连接docker
    public static DockerClient connect() throws URISyntaxException {
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

    @Test
    void dockerInfo() throws URISyntaxException {
        DockerClient client = connect();
        Info info = client.infoCmd().exec();
        System.out.println("docker info : " + info.toString());
    }

    //创建容器
    public static CreateContainerResponse createContainer(DockerClient client) throws URISyntaxException {


        String containerName;
        int containerExportPort;
        int hostBindPort;
        List<String> volumeList;

        CreateContainerCmd containerCmd = client.createContainerCmd("jupyter_cus:1.0")
            //名字
            .withName("jupyter_cus3")
            //端口映射 内部80端口与外部81端口映射
            // .withHostConfig(new HostConfig().withPortBindings(new Ports(new ExposedPort(80), Ports.Binding.bindPort(81))))
            .withHostConfig(new HostConfig().withPortBindings(new Ports(new ExposedPort(8888), Ports.Binding.bindPort(8813))))
            //环境变量
            // .withEnv("key=value")
            //挂载
            .withBinds(
                Bind.parse("/e/DockerRepository/jupyter_workspace:/opt/notebooks"),
                Bind.parse("/e/DockerRepository/jupyter_config/jupyter_lab_config.py:/jupyter_lab_config.py")
            );

        //创建
        CreateContainerResponse response = containerCmd.exec();
        System.out.println(response.getId());
        return response;
    }

    @Test
    void startContainer() throws URISyntaxException {
        DockerClient client = connect();
        //创建
        CreateContainerResponse response = createContainer(client);
        String containerId = response.getId();
        //启动
        client.startContainerCmd(containerId).exec();
    }

    @Autowired
    IWorkspaceService workspaceService;

    @Autowired
    DockerOperMapper dockerOperMapper;

    //测试mapper层list转string
    @Test
    void testMapperList2String(){
        //插入
        Boolean success = workspaceService.initWorkspace(1L);
        System.out.println(success);

        //查找
        // List<JupyterContainer> jupyterContainers = dockerOperMapper.listAll();
        // System.out.println(jupyterContainers);


    }

    // 将数据卷目录修改成适合docker的格式 [ /e/... ]
    @Test
    void formatPathSupportDocker(){
        // E:\opengms-lab\container\workspace\test:/opt/notebooks
        String path = "E:\\opengms-lab\\container\\workspace\\test:/opt/notebooks";
        path = path.replaceAll("\\\\","/");
        int index = path.indexOf(":");
        String outputPath = path;
        if (index == 1){
            // 只有 E: 这种形式的才要进行处理
            outputPath = "/" + Character.toString(path.charAt(0)).toLowerCase() + path.substring(index + 1);
        }
        System.out.println(outputPath);



    }

}
