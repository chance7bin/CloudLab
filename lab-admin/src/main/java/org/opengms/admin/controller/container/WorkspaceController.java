package org.opengms.admin.controller.container;

import org.opengms.admin.clients.container.WorkspaceClient;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.po.system.SysUser;
import org.opengms.admin.service.ISysUserService;
import org.opengms.admin.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作空间接口
 *
 * @author bin
 * @date 2022/10/6
 */
@RestController
@RequestMapping("/container/workspace")
public class WorkspaceController {

    @Autowired
    WorkspaceClient workspaceClient;

    @Autowired
    ISysUserService sysUserService;

    @GetMapping("/initialization/{imageId}/{containerName}")
    public ApiResponse initWorkspace(
        @PathVariable("imageId") String imageId,
        @PathVariable("containerName") String containerName){

        Long userId = SecurityUtils.getUserId();
        SysUser sysUser = sysUserService.selectUserById(userId);
        return workspaceClient.initWorkspace(sysUser.getUserName(), imageId, containerName);

    }


    @GetMapping(value = "/dir/{containerId}")
    public ApiResponse listWorkspaceDirByContainerId(
        @PathVariable(value = "containerId") String containerId) {

        return workspaceClient.listWorkspaceDirByContainerId(containerId);
    }

    /**
     * 根据containerId获取工作空间信息
     **/
    @GetMapping(value = "/{containerId}")
    public ApiResponse getWorkspaceByContainerId(@PathVariable("containerId")Long containerId) {
        return workspaceClient.getWorkspaceByContainerId(containerId);
    }

}
