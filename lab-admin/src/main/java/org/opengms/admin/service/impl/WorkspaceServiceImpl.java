package org.opengms.admin.service.impl;

import com.github.dockerjava.api.exception.InternalServerErrorException;
import org.apache.poi.ss.formula.functions.T;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.opengms.admin.entity.bo.docker.LaunchParams;
import org.opengms.admin.entity.po.docker.ContainerInfo;
import org.opengms.admin.entity.po.docker.JupyterContainer;
import org.opengms.admin.entity.po.system.SysUser;
import org.opengms.admin.exception.ServiceException;
import org.opengms.admin.mapper.DockerOperMapper;
import org.opengms.admin.mapper.SysUserMapper;
import org.opengms.admin.service.IDockerService;
import org.opengms.admin.service.IJupyterService;
import org.opengms.admin.service.IWorkspaceService;
import org.opengms.common.utils.file.FileUtils;
import org.opengms.common.utils.ip.IpUtils;
import org.opengms.common.utils.uuid.SnowFlake;
import org.opengms.common.utils.uuid.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.AccessType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
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

    @Autowired
    SysUserMapper sysUserMapper;


    @Value(value = "${lab.repository.workspace}")
    private String workspace;

    private static final String INNER_CONFIG_PATH = "/jupyter_lab_config.py";
    private static final String INNER_WORKSPACE_DIR = "/opt/notebooks";
    private static final int JUPYTER_CONTAINER_EXPORT_PORT = 8888;
    private static final int[] HOST_BIND_PORT_LIMIT = {7001, 8000};


    @Override
    public boolean initWorkspace(Long userId, String imageName) {

        // 工作空间的用户文件存放在 lab.repository.container 目录的workspace文件夹下
        // 工作空间的jupyter配置文件存放在 lab.repository.container 目录的config文件夹下
        String configDir = workspace + "/config";
        String workspaceDir = workspace + "/workspace";

        //初始化jupyter实例
        JupyterContainer jupyterContainer = new JupyterContainer();
        jupyterContainer.setContainerId(SnowFlake.nextId());
        // jupyterContainer.setImageName("jupyter_cus:5.0");
        jupyterContainer.setImageName(imageName);
        jupyterContainer.setContainerName(jupyterContainer.getImageName().replaceAll(":","_") + "_" + jupyterContainer.getContainerId());
        jupyterContainer.setContainerExportPort(JUPYTER_CONTAINER_EXPORT_PORT);

        // 创建者为该登录用户
        SysUser sysUser = sysUserMapper.selectUserById(userId);
        if (sysUser != null){
            jupyterContainer.setCreateBy(sysUser.getUserName());
        }


        // 生成jupyter的配置文件
        String jupyterToken = UUID.fastUUID().toString();
        jupyterContainer.setJupyterToken(jupyterToken);
        String configPath = jupyterService.generateJupyterConfig(configDir, jupyterContainer.getContainerId().toString(), jupyterToken);
        if (configPath == null){
            return false;
        }
        // 生成工作空间
        String workspacePath = workspaceDir + File.separator + jupyterContainer.getContainerName();

        //设置绑定的主机端口
        // jupyterContainer.setHostBindPort(8826);
        List<Integer> usedPort = dockerOperMapper.listAllUsedPort();
        List<Integer> excludePort = new ArrayList<>(usedPort);
        boolean allocated = setHostBindPort(jupyterContainer, "localhost",HOST_BIND_PORT_LIMIT, excludePort);
        if (!allocated){
            throw new ServiceException("无可分配端口");
        }

        // 解决 "Invalid volume specification" 问题，需将路径改成 /e/...
        jupyterContainer.setConfigVolume(formatPathSupportDocker(configPath + ":" + INNER_CONFIG_PATH));
        jupyterContainer.setWorkspaceVolume(formatPathSupportDocker(workspacePath + ":" + INNER_WORKSPACE_DIR));


        List<String> volumeList = jupyterContainer.getVolumeList();
        volumeList.add(jupyterContainer.getConfigVolume());
        volumeList.add(jupyterContainer.getWorkspaceVolume());

        //对创建容器抛出的异常做处理
        try {
            // 启动容器
            dockerService.createContainer(jupyterContainer);
        } catch (InternalServerErrorException serverErrorException){
            if (serverErrorException.getMessage().contains("port is already allocated")){
                throw new ServiceException("端口被占用");
            }
            else {
                throw new ServiceException(serverErrorException.getMessage());
            }
        } catch (Exception e){
            throw new ServiceException(e.getMessage());
        }

        int count = dockerOperMapper.insertJupyterContainer(jupyterContainer);

        return count > 0;

    }



    /**
     * 设置jupyter绑定的主机端口
     * @param jupyterContainer
     * @return boolean 是否有端口可以分配
     * @Author bin
     **/
    // TODO: 2022/10/12 如何选择绑定的端口(还需添加一张表记录该主机已经使用的端口，不然每次都要进行尝试连接的操作，很浪费时间)
    private boolean setHostBindPort(JupyterContainer jupyterContainer, String host,int[] portLimit, List<Integer> excludePort){
        for (int port = portLimit[0]; port < portLimit[1]; port++) {
            if (!excludePort.contains(port)){
                //判断端口是否被占用
                if (IpUtils.isSocketOccupy(host, port)){
                    // TODO: 2022/10/12 是否需要将该记录加入到host_port表中
                    // excludePort.add(port);
                }
                else {
                    jupyterContainer.setHostBindPort(port);
                    return true;
                }
            }
        }
        return false;
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
