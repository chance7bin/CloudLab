package org.opengms.container;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.opengms.common.TerminalRes;
import org.opengms.common.utils.TerminalUtils;
import org.opengms.common.utils.XMLUtils;
import org.opengms.common.utils.uuid.SnowFlake;
import org.opengms.container.entity.bo.mdl.ModelClass;
import org.opengms.container.entity.dto.docker.ImageInfoDTO;
import org.opengms.container.entity.po.docker.ImageInfo;
import org.opengms.container.mapper.ImageMapper;
import org.opengms.container.service.IDockerService;
import org.opengms.container.service.IMdlService;
import org.opengms.container.service.IWorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Docker测试代码
 *
 * @author 7bin
 * @date 2023/04/26
 */
@Slf4j
@SpringBootTest
public class DockerTests {

    @Autowired
    IDockerService dockerService;


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

    // docker信息
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


    // 将数据卷目录修改成适合docker的格式 [ /e/... ]
    @Test
    void formatPathSupportDocker() {
        // E:\opengms-lab\container\workspace\test:/opt/notebooks
        String path = "E:\\opengms-lab\\container\\workspace\\test:/opt/notebooks";
        path = path.replaceAll("\\\\", "/");
        int index = path.indexOf(":");
        String outputPath = path;
        if (index == 1) {
            // 只有 E: 这种形式的才要进行处理
            outputPath = "/" + Character.toString(path.charAt(0)).toLowerCase() + path.substring(index + 1);
        }
        System.out.println(outputPath);


    }


    //测试获取镜像列表
    @Test
    void testListImages() {
        // log(isSocketAliveUitlitybyCrunchify("localhost", 27017));
        List<ImageInfo> list = dockerService.listImages();
        // List list = dockerService.listContainers();

        System.out.println(list);


    }

    String dockerRegistryUrl = "172.21.212.177:8080";

    //测试push镜像
    @Test
    void testPushImages() throws InterruptedException {

        String imageName = "jupyter_ws";
        String tag = "1.0";

        dockerService.pushImage(imageName, tag);
    }

    String tagImage(String imageNameWithTag, String newImageNameWithRepository, String newTag) {

        final String id = dockerClient.inspectImageCmd(imageNameWithTag)
            .exec()
            .getId();

        // push the image to the registry
        dockerClient.tagImageCmd(id, newImageNameWithRepository, newTag).exec();

        return newImageNameWithRepository + ":" + newTag;
    }

    //测试pull镜像
    @Test
    void testPullImages() throws InterruptedException {

        String imageName = "nginx";
        String tag = "3.0";
        String newImageWithTag = dockerRegistryUrl + "/" + imageName + ":" + tag;

        // 1.使用dockerclient拉取镜像
        dockerClient.pullImageCmd(newImageWithTag)
            .start()
            .awaitCompletion(1, TimeUnit.MINUTES);

        // 2.重命名(docker tag)镜像，删除镜像仓库地址前缀
        tagImage(newImageWithTag, "ogms/" + imageName, tag);


        // 3.删除以镜像仓库地址为前缀的镜像
        dockerClient.removeImageCmd(newImageWithTag).withForce(true).exec();

        System.out.println();


    }


    //commit容器
    @Test
    void testCommitContainer() {
        // dockerService.commitContainer();
        String container = "test6";
        String outputPath = "E:\\opengms-lab\\container\\package\\test6.tar";

        dockerService.exportContainer(container, outputPath);
        // dockerService.importContainer();
    }

    @Autowired
    DockerClient dockerClient;

    @Autowired
    ImageMapper imageMapper;

    // 添加基础镜像
    @Test
    void addDefaultImage() {
        ImageInfo imageInfo = new ImageInfo();
        imageInfo.setId(SnowFlake.nextId());
        imageInfo.setImageId("sha256:8834a83ccd002170cc8b60bfee25582aa44b3b2b444dd94fada1e6d6e3eb3326");
        imageInfo.setImageName("jupyter_ws");
        imageInfo.setTag("3.0");
        imageInfo.setSize(391766465L);
        imageInfo.setRepoTags("jupyter_ws:3.0");
        imageMapper.insert(imageInfo);
    }

    @Autowired
    IMdlService mdlService;

    @Test
    void parseMdlFile() throws Exception {
        String mdlPath = "E:\\opengms-lab\\container\\workspace\\8334486711696420864\\data\\createWordCloud.mdl";


        // ModelClass modelClass = mdlService.parseMdlFile(mdlPath);
        ModelClass m = (ModelClass) XMLUtils.convertXmlFileToObject(ModelClass.class, mdlPath);

        System.out.println();
    }

    // 容器测试
    @Test
    void containerTest() {

        // String status = dockerService.getContainerStatusByContainerInsId("5f760de92543d46726ef013f6cc0c22ae3a13a349c8a831399c966412cd574c6");
        //
        //
        // List<Container> containers = dockerService.listContainers();
        // System.out.println();

        // ImageInfo imageInfoByRepoTags = imageMapper.getImageInfoByRepoTags("newimage3:1.0");
        // System.out.println();

        // dockerService.commitContainer();

        dockerService.inspectImage("sha256:59149c73a68c19b242b6b4cce1e52deb36802aa2c0c65d2990c5eb1aff6e062e");

    }


    @Test
    void inspectImage() throws InterruptedException {

        String sha256 = "sha256:8834a83ccd002170cc8b60bfee25582aa44b3b2b444dd94fada1e6d6e3eb3326";

        try {
            ImageInfo imageInfo = dockerService.inspectImage(sha256);
        } catch (Exception ex){
            // docker中没有该image到hub上拉
            dockerService.pullImage("jupyter_ws", "1.0");
        }


        System.out.println();


    }

    @Test
    void updateImageStatus(){
        imageMapper.updateStatusById(8344298234506244096L, "error");
    }

    @Test
    void logTest(){
        log.info("[程序正常退出] 退出码 : {} (response: {})", 1, "success");
    }
}
