package org.opengms.admin.service.impl;

import org.opengms.admin.entity.bo.docker.LaunchParams;
import org.opengms.admin.entity.po.docker.ContainerInfo;
import org.opengms.admin.entity.po.docker.JupyterContainer;
import org.opengms.admin.mapper.DockerOperMapper;
import org.opengms.admin.service.IDockerService;
import org.opengms.admin.service.IJupyterService;
import org.opengms.admin.service.IWorkspaceService;
import org.opengms.common.utils.file.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.AccessType;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 工作空间服务层
 *
 * @author bin
 * @date 2022/10/6
 */
@Service
public class WorkspaceServiceImpl implements IWorkspaceService {

    @Autowired
    IJupyterService jupyterService;

    @Autowired
    IDockerService dockerService;

    @Autowired
    DockerOperMapper dockerOperMapper;


    @Value(value = "${lab.repository.workspace}")
    private String workspace;

    private static final String INNER_CONFIG_PATH = "/jupyter_lab_config.py";
    private static final String INNER_WORKSPACE_DIR = "/opt/notebooks";


    @Override
    public Boolean initWorkspace(Long userId) {

        // 工作空间的用户文件存放在 lab.repository.container 目录的workspace文件夹下
        // 工作空间的jupyter配置文件存放在 lab.repository.container 目录的config文件夹下

        // 生成jupyter的配置文件
        String configDir = workspace + "/config";
        String configPath = jupyterService.generateJupyterConfig(configDir);

        if (configPath == null){
            return false;
        }

        // 启动jupyter服务
        JupyterContainer jupyterContainer = new JupyterContainer();
        jupyterContainer.setImageName("jupyter_cus:5.0");
        jupyterContainer.setContainerName(jupyterContainer.getImageName().replaceAll(":","_") + "_" + jupyterContainer.getContainerId());
        jupyterContainer.setContainerExportPort(8888);
        jupyterContainer.setHostBindPort(8825);
        jupyterContainer.setConfigVolume(formatPathSupportDocker(configPath + ":" + INNER_CONFIG_PATH));
        jupyterContainer.setWorkspaceVolume(formatPathSupportDocker("E:\\opengms-lab\\container\\workspace\\test" + ":" + INNER_WORKSPACE_DIR));

        // 解决 "Invalid volume specification" 问题，需将路径改成 /e/...


        List<String> volumeList = jupyterContainer.getVolumeList();
        volumeList.add(jupyterContainer.getConfigVolume());
        volumeList.add(jupyterContainer.getWorkspaceVolume());


        ContainerInfo container = dockerService.createContainer(jupyterContainer);

        int count = dockerOperMapper.insertJupyterContainer(jupyterContainer);

        return count > 0;

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


    /**
     * 构造容器启动参数
     * @param jupyterContainer
     * @return LaunchParams
     * @Author bin
     **/
    private LaunchParams buildLaunchParams(JupyterContainer jupyterContainer){
        LaunchParams launchParams = new LaunchParams();
        launchParams.setImageName(jupyterContainer.getImageName());
        launchParams.setContainerExportPort(jupyterContainer.getContainerExportPort());
        launchParams.setHostBindPort(jupyterContainer.getHostBindPort());
        List<String> volumeList = launchParams.getVolumeList();
        volumeList.add(jupyterContainer.getConfigVolume());
        volumeList.add(jupyterContainer.getWorkspaceVolume());

        return launchParams;

    }


}
