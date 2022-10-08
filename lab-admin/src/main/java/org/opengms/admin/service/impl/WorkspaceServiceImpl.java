package org.opengms.admin.service.impl;

import org.opengms.admin.entity.po.docker.JupyterContainer;
import org.opengms.admin.service.IDockerService;
import org.opengms.admin.service.IJupyterService;
import org.opengms.admin.service.IWorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.AccessType;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceServiceImpl implements IWorkspaceService {

    @Autowired
    IJupyterService jupyterService;

    @Autowired
    IDockerService dockerService;



    @Value(value = "${lab.repository.workspace}")
    private String workspace;


    @Override
    public Boolean initWorkspace(Long userId) {

        // 工作空间的用户文件存放在 lab.repository.workspace 目录的user文件夹下
        // 工作空间的jupyter配置文件存放在 lab.repository.workspace 目录的config文件夹下

        // 生成jupyter的配置文件
        String configDir = workspace + "/config";
        Boolean generateConfigDone = jupyterService.generateJupyterConfig(configDir);

        if (!generateConfigDone){
            return false;
        }

        // 启动jupyter服务
        JupyterContainer jupyterContainer = new JupyterContainer();
        jupyterContainer.setImageName("jupyter_cus:1.0");
        dockerService.createContainer(jupyterContainer);

        return generateConfigDone;

    }
}
