package org.opengms.container.service.impl;

import cn.hutool.core.io.FileUtil;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import org.opengms.common.utils.file.FileUtils;
import org.opengms.common.utils.ip.IpUtils;
import org.opengms.common.utils.uuid.SnowFlake;
import org.opengms.common.utils.uuid.UUID;
import org.opengms.container.constant.ContainerConstants;
import org.opengms.container.entity.bo.docker.LaunchParams;
import org.opengms.container.entity.dto.docker.JupyterInfoDTO;
import org.opengms.container.entity.dto.workspace.TreeDTO;
import org.opengms.container.entity.po.docker.JupyterContainer;
import org.opengms.container.exception.ServiceException;
import org.opengms.container.mapper.DockerOperMapper;
import org.opengms.container.service.IDockerService;
import org.opengms.container.service.IJupyterService;
import org.opengms.container.service.IWorkspaceService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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


    @Value(value = "${container.repository}")
    private String repository;

    private static final int JUPYTER_CONTAINER_EXPORT_PORT = 8888;
    private static final int[] HOST_BIND_PORT_LIMIT = {7001, 8000};


    @Override
    public boolean initWorkspace(String username, String imageName, String containerName) {


        //初始化jupyter实例
        JupyterContainer jupyterContainer = new JupyterContainer();
        jupyterContainer.setContainerId(SnowFlake.nextId());
        // jupyterContainer.setImageName("jupyter_cus:5.0");
        jupyterContainer.setImageName(imageName);
        // jupyterContainer.setContainerName(jupyterContainer.getImageName().replaceAll(":","_") + "_" + jupyterContainer.getContainerId());
        jupyterContainer.setContainerName(containerName);
        jupyterContainer.setContainerExportPort(JUPYTER_CONTAINER_EXPORT_PORT);

        // 创建者为该登录用户
        jupyterContainer.setCreateBy(username);


        // 工作空间的jupyter配置文件存放在 container.repository 目录的pod/{containerId}/config文件夹下
        // String configDir = "/workspace/" + jupyterContainer.getContainerId() + "/config";
        String configDir = ContainerConstants.configDir(jupyterContainer.getContainerId());
        // 工作空间的用户文件存放在 container.repository 目录的pod/{containerId}/data文件夹下
        // String workspaceDir = "/workspace/" + jupyterContainer.getContainerId() + "/data";
        String workspaceDir = ContainerConstants.workspaceDir(jupyterContainer.getContainerId());
        // 该工作空间所在的容器创建的模型服务都在 container.repository 目录的 workspace/{containerId}/service 下
        // String serviceDir = "/workspace/" + jupyterContainer.getContainerId() + "/service";
        String serviceDir = ContainerConstants.serviceDir(jupyterContainer.getContainerId());

        // 生成jupyter的配置文件
        String jupyterToken = UUID.fastUUID().toString();
        jupyterContainer.setJupyterToken(jupyterToken);
        String configPath = jupyterService.generateJupyterConfig(repository + configDir, jupyterContainer.getContainerId().toString(), jupyterToken);
        if (configPath == null){
            return false;
        } else {
            configPath = (configPath.split(repository))[1];
        }
        // 生成工作空间
        String workspacePath = workspaceDir;

        //设置绑定的主机端口
        // jupyterContainer.setHostBindPort(8826);
        List<Integer> usedPort = dockerOperMapper.listAllUsedPort();
        List<Integer> excludePort = new ArrayList<>(usedPort);
        boolean allocated = setHostBindPort(jupyterContainer, "localhost",HOST_BIND_PORT_LIMIT, excludePort);
        if (!allocated){
            throw new ServiceException("无可分配端口");
        }

        // 解决 "Invalid volume specification" 问题，需将路径改成 /e/...
        // jupyterContainer.setConfigVolume(formatPathSupportDocker(configPath + ":" + INNER_CONFIG_PATH));
        // jupyterContainer.setWorkspaceVolume(formatPathSupportDocker(workspacePath + ":" + INNER_WORKSPACE_DIR));
        // jupyterContainer.setServiceVolume(formatPathSupportDocker(serviceDir + ":" + INNER_SERVICE_DIR));
        jupyterContainer.setConfigVolume(configPath + ":" + ContainerConstants.INNER_CONFIG_PATH);
        jupyterContainer.setWorkspaceVolume(workspacePath + ":" + ContainerConstants.INNER_WORKSPACE_DIR);
        jupyterContainer.setServiceVolume(serviceDir + ":" + ContainerConstants.INNER_SERVICE_DIR);


        List<String> volumeList = jupyterContainer.getVolumeList();
        volumeList.add(jupyterContainer.getConfigVolume());
        volumeList.add(jupyterContainer.getWorkspaceVolume());
        volumeList.add(jupyterContainer.getServiceVolume());

        //对创建容器抛出的异常做处理
        try {
            // 启动容器
            jupyterContainer.setCmd(ContainerConstants.RUN_JUPYTER_CMD);
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


    @Override
    public List<TreeDTO> getFileInfoByPathContainChildren(String path){
        return getFileInfoByPathContainChildren(path, path);
    }

    public List<TreeDTO> getFileInfoByPathContainChildren(String path, String rootPath){
        if (rootPath == null){
            rootPath = path;
        }

        List<File> files = FileUtils.ls(path);
        List<TreeDTO> fileDTOList = new ArrayList<>();
        for (File file : files) {
            TreeDTO fileDTO = new TreeDTO();
            fileDTO.setLabel(file.getName());
            fileDTO.setRelativePath(FileUtils.getFileRelativePath(file,rootPath));
            // fileDTO.setDirectory(file.isDirectory());
            if (!file.isDirectory()){
                // fileDTO.setFileType(FileTypeUtils.getFileType(file));
                // fileDTO.setFileSize(FileUtils.calcSize(FileUtil.size(file)));
            } else {
                fileDTO.setChildren(getFileInfoByPathContainChildren(FileUtil.getCanonicalPath(file), rootPath));
            }
            fileDTOList.add(fileDTO);

        }
        return fileDTOList;
    }



    @Override
    public JupyterInfoDTO getJupyterContainerById(Long id) {

        JupyterContainer jc = dockerOperMapper.getJupyterContainerInfoById(id);
        JupyterInfoDTO jupyterInfoDTO = new JupyterInfoDTO();
        if (jc != null){
            BeanUtils.copyProperties(jc, jupyterInfoDTO);
        }
        return jupyterInfoDTO;

    }

}
