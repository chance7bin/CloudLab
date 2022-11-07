package org.opengms.admin;

import cn.hutool.core.io.FileUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.Test;
import org.opengms.admin.drive.entity.dto.FileDTO;
import org.opengms.admin.entity.bo.Server;
import org.opengms.admin.mapper.DockerOperMapper;
import org.opengms.admin.msc.entity.bo.mdl.*;
import org.opengms.admin.msc.entity.bo.mdl.Event;
import org.opengms.admin.msc.enums.MDLStructure;
import org.opengms.admin.service.IDockerService;
import org.opengms.admin.service.IWorkspaceService;
import org.opengms.common.utils.ReflectUtils;
import org.opengms.common.utils.file.FileTypeUtils;
import org.opengms.common.utils.file.FileUtils;
import org.opengms.common.utils.ip.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author bin
 * @date 2022/09/22
 */
@Slf4j
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

    //测试启动容器
    @Test
    void testStartContainer(){
        //插入
        Boolean success = workspaceService.initWorkspace(1L, "jupyter_cus:5.0");
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


    //测试端口是否被占用
    @Test
    void testHost(){
        // log(isSocketAliveUitlitybyCrunchify("localhost", 27017));

        log(isSocketAlive("localhost", 8080));

    }

    /**
     * 判断主机端口是否正在使用
     *
     * @param hostName
     * @param port
     * @return boolean - true/false
     */
    public static boolean isSocketAlive(String hostName, int port) {
        boolean isAlive = false;

        // 创建一个套接字
        SocketAddress socketAddress = new InetSocketAddress(hostName, port);
        Socket socket = new Socket();

        // 超时设置，单位毫秒
        int timeout = 1000;

        // log("hostName: " + hostName + ", port: " + port);
        try {
            socket.connect(socketAddress, timeout);
            socket.close();
            isAlive = true;

        } catch (SocketTimeoutException exception) {
            // System.out.println("SocketTimeoutException " + hostName + ":" + port + ". " + exception.getMessage());
        } catch (IOException exception) {
            // System.out.println(
            //     "IOException - Unable to connect to " + hostName + ":" + port + ". " + exception.getMessage());
        }
        return isAlive;
    }

    private static void log(String string) {
        System.out.println(string);
    }

    private static void log(boolean isAlive) {
        System.out.println("是否真正在使用: " + isAlive + "\n");
    }

    @Autowired
    IDockerService dockerService;

    //测试获取镜像列表
    @Test
    void testListImages(){
        // log(isSocketAliveUitlitybyCrunchify("localhost", 27017));
        // List list = dockerService.listImages();
        List list = dockerService.listContainers();

        System.out.println(list);


    }

    //测试事务
    @Test
    void testTransaction(){
        // log.info("123");
    }

    @Value(value = "${lab.repository.workspace}")
    private String workspace;


    //测试获取文件列表
    @Test
    void testGetFilesContainChild(){
        String workspaceDir = workspace + "/workspace";
        List<File> files = FileUtils.ls(workspaceDir);
        List<FileDTO> fileDTOList = new ArrayList<>();
        for (File file : files) {
            FileDTO fileDTO = new FileDTO();
            fileDTO.setFilename(file.getName());
            fileDTO.setDirectory(file.isDirectory());
            if (!file.isDirectory()){
                fileDTO.setFileType(FileTypeUtils.getFileType(file));
                fileDTO.setFileSize(FileUtils.calcSize(FileUtil.size(file)));
            }
            fileDTOList.add(fileDTO);
        }
        System.out.println(fileDTOList);
    }


    //测试读取xml
    @Test
    void testXML() throws DocumentException, NoSuchFieldException, InstantiationException, IllegalAccessException {



        SAXReader saxReader = new SAXReader();

        Document document = saxReader.read(new File("E:\\Projects\\pythonProject\\ogmslab\\test\\createWordCloud.mdl"));

        // 获取ModelClass
        ModelClass modelClass = new ModelClass();
        Element modelClassNode = document.getRootElement();
        setElementAttributes(modelClassNode.attributes(), modelClass);

        // 获取AttributeSet
        AttributeSet attributeSet = new AttributeSet();
        Element attributeSetNode = modelClassNode.element(MDLStructure.ATTRIBUTE_SET.getInfo());
        Description description = new Description();
        Element descriptionNode = attributeSetNode.element(MDLStructure.DESCRIPTION.getInfo());
        setElementAttributes(descriptionNode.attributes(), description);
        attributeSet.setDescription(description);
        modelClass.setAttributeSet(attributeSet);

        // 获取Behavior
        Behavior behavior = new Behavior();
        Element behaviorNode = modelClassNode.element(MDLStructure.BEHAVIOR.getInfo());
        StateGroup stateGroup = new StateGroup();
        Element stateGroupNode = behaviorNode.element(MDLStructure.STATE_GROUP.getInfo());
        // 获取state
        List<Element> states = stateGroupNode.elements(MDLStructure.STATE.getInfo());
        List<State> stateList = new ArrayList<>();
        for (Element stateNode : states) {
            State state = new State();
            setElementAttributes(stateNode.attributes(), state);
            List<Element> events = stateNode.elements(MDLStructure.EVENT.getInfo());
            List<Event> eventList = new ArrayList<>();
            // 获取event
            for (Element eventNode : events) {
                Event event = new Event();
                setElementAttributes(eventNode.attributes(), event);
                Element inputParameterNode = eventNode.element(MDLStructure.INPUT_PARAMETER.getInfo());
                if (inputParameterNode != null){
                    InputParameter inputParameter = new InputParameter();
                    setElementAttributes(inputParameterNode.attributes(), inputParameter);
                    event.setInputParameter(inputParameter);
                }
                Element outputParameterNode = eventNode.element(MDLStructure.OUTPUT_PARAMETER.getInfo());
                if (outputParameterNode != null){
                    OutputParameter outputParameter = new OutputParameter();
                    setElementAttributes(outputParameterNode.attributes(), outputParameter);
                    event.setOutputParameter(outputParameter);
                }
                eventList.add(event);
            }
            state.setEvents(eventList);
            stateList.add(state);
        }

        stateGroup.setStates(stateList);
        behavior.setStateGroup(stateGroup);
        modelClass.setBehavior(behavior);

        System.out.println(modelClass);


    }


    //设置xml的属性
    private void setElementAttributes(List<Attribute> attributes, Object obj){
        for (Attribute attribute : attributes) {
            try {
                ReflectUtils.setValueByProp(obj,attribute.getName(),attribute.getValue());
            } catch (Exception e){
                log.error(e.getMessage());
            }
        }
    }



}
