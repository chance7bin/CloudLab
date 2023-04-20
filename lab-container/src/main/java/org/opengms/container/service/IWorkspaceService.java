package org.opengms.container.service;


import org.opengms.container.entity.dto.docker.JupyterInfoDTO;
import org.opengms.container.entity.dto.workspace.TreeDTO;

import java.util.List;

/**
 * 工作空间接口
 *
 * @author bin
 * @date 2022/10/6
 */
public interface IWorkspaceService {

    /**
     * 初始化工作空间
     * @param username 用户名
     * @param imageId 镜像id
     * @param containerName 容器名
     * @return 是否初始化成功
     * @author bin
     **/
    boolean initWorkspace(String username, String imageId, String containerName);

    /**
     * 获取路径下的文件列表（包含子目录）
     *
     * @param path 查询路径
     * @return {@link List<TreeDTO>}
     * @author 7bin
     **/
    List<TreeDTO> getFileInfoByPathContainChildren(String path);


}
