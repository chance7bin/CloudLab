package org.opengms.container.service.impl;

import cn.hutool.core.io.FileUtil;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import org.opengms.common.utils.file.FileUtils;
import org.opengms.common.utils.ip.IpUtils;
import org.opengms.common.utils.uuid.SnowFlake;
import org.opengms.common.utils.uuid.UUID;
import org.opengms.container.constant.ContainerConstants;
import org.opengms.container.constant.ContainerStatus;
import org.opengms.container.entity.bo.docker.LaunchParams;
import org.opengms.container.entity.dto.workspace.TreeDTO;
import org.opengms.container.entity.po.JupyterContainer;
import org.opengms.container.entity.po.docker.ImageInfo;
import org.opengms.container.enums.ContainerType;
import org.opengms.container.exception.ServiceException;
import org.opengms.container.mapper.ContainerRelationMapper;
import org.opengms.container.mapper.ImageMapper;
import org.opengms.container.mapper.JupyterMapper;
import org.opengms.container.service.IContainerService;
import org.opengms.container.service.IDockerService;
import org.opengms.container.service.IJupyterService;
import org.opengms.container.service.IWorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    JupyterMapper jupyterMapper;

    @Autowired
    ContainerRelationMapper containerRelationMapper;

    @Autowired
    IContainerService containerService;

    @Autowired
    ImageMapper imageMapper;

    @Value(value = "${docker.clientHost}")
    private String clientHost;

    @Value(value = "${docker.clientMac}")
    private String clientMac;


    @Value(value = "${container.repository}")
    private String repository;

    private static final int JUPYTER_CONTAINER_EXPORT_PORT = 8888;
    private static final int[] HOST_BIND_PORT_LIMIT = {7001, 8000};


    @Override
    // @Transactional(rollbackFor = Exception.class)
    public boolean initWorkspace(String username, String imageId, String containerName) {


        //初始化jupyter实例
        JupyterContainer jupyterContainer = new JupyterContainer();
        jupyterContainer.setContainerId(SnowFlake.nextId());
        // jupyterContainer.setImageName("jupyter_cus:5.0");
        ImageInfo imageInfo = imageMapper.selectById(Long.valueOf(imageId));
        jupyterContainer.setImageName(imageInfo.getRepoTags());
        jupyterContainer.setImageId(imageInfo.getId());
        // jupyterContainer.setContainerName(jupyterContainer.getImageName().replaceAll(":","_") + "_" + jupyterContainer.getContainerId());
        jupyterContainer.setContainerName(containerName);
        jupyterContainer.setContainerExportPort(JUPYTER_CONTAINER_EXPORT_PORT);

        // 创建者为该登录用户
        jupyterContainer.setCreateBy(username);


        // 工作空间的jupyter配置文件存放在 container.repository 目录的 pod/{containerId}/config 文件夹下
        // String configDir = "/workspace/" + jupyterContainer.getContainerId() + "/config";
        String configDir = ContainerConstants.CONFIG_DIR(jupyterContainer.getContainerId());
        // 工作空间的用户文件存放在 container.repository 目录的 pod/{containerId}/data 文件夹下
        // String workspaceDir = "/workspace/" + jupyterContainer.getContainerId() + "/data";
        String workspaceDir = ContainerConstants.DATA_DIR(jupyterContainer.getContainerId());
        // 该工作空间所在的容器创建的模型服务都在 container.repository 目录的 workspace/{containerId}/service 下
        // String SERVICE_DIR = "/workspace/" + jupyterContainer.getContainerId() + "/service";
        // String serviceDir = ContainerConstants.SERVICE_DIR(jupyterContainer.getContainerId());

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

        // 设置绑定的主机端口
        // jupyterContainer.setHostBindPort(8826);
        String hostIP = clientHost;
        List<Integer> usedPort = containerRelationMapper.listUsedPortByIP(hostIP);
        List<Integer> excludePort = new ArrayList<>(usedPort);
        boolean allocated = setHostBindPort(jupyterContainer, hostIP, HOST_BIND_PORT_LIMIT, excludePort);
        if (!allocated){
            throw new ServiceException("无可分配端口");
        }
        // 绑定主机和端口
        jupyterContainer.setHostIP(hostIP);
        jupyterContainer.setHostMAC(clientMac);


        // 解决 "Invalid volume specification" 问题，需将路径改成 /e/...
        // jupyterContainer.setConfigVolume(formatPathSupportDocker(configPath + ":" + INNER_CONFIG_PATH));
        // jupyterContainer.setWorkspaceVolume(formatPathSupportDocker(workspacePath + ":" + INNER_WORKSPACE_DIR));
        // jupyterContainer.setServiceVolume(formatPathSupportDocker(SERVICE_DIR + ":" + INNER_SERVICE_DIR));
        jupyterContainer.setConfigVolume(configPath + ":" + ContainerConstants.INNER_CONFIG_PATH);
        jupyterContainer.setWorkspaceVolume(workspacePath + ":" + ContainerConstants.INNER_WORKSPACE_DIR);
        // jupyterContainer.setServiceVolume(serviceDir + ":" + ContainerConstants.INNER_SERVICE_DIR);


        List<String> volumeList = jupyterContainer.getVolumeList();
        volumeList.add(jupyterContainer.getConfigVolume());
        volumeList.add(jupyterContainer.getWorkspaceVolume());
        // volumeList.add(jupyterContainer.getServiceVolume());

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

        jupyterContainer.setStatus(dockerService.getContainerStatusByContainerInsId(jupyterContainer.getContainerInsId()));
        int count = containerService.insertContainer(jupyterContainer, ContainerType.JUPYTER);

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


}
