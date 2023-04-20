package org.opengms.container.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.opengms.container.entity.dto.ApiResponse;
import org.opengms.container.entity.dto.workspace.TreeDTO;
import org.opengms.container.service.IJupyterService;
import org.opengms.container.service.IWorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 工作空间接口
 *
 * @author bin
 * @date 2022/10/6
 */
@Api(value = "/workspace", tags = {"工作空间"})
@RestController
@RequestMapping("/workspace")
public class WorkspaceController {

    @Value(value = "${container.repository}")
    private String repository;

    @Autowired
    IJupyterService jupyterService;

    @Autowired
    IWorkspaceService workspaceService;

    @GetMapping("/initialization/{username}/{imageId}/{containerName}")
    public ApiResponse initWorkspace(
        @PathVariable("username") String username,
        @PathVariable("imageId") String imageId,
        @PathVariable("containerName") String containerName){

        // Long userId = SecurityUtils.getUserId();
        return workspaceService.initWorkspace(username, imageId, containerName) ? ApiResponse.success() : ApiResponse.error();

    }


    @ApiOperation(value = "列出工作空间的文件目录", notes = "", httpMethod = "GET")
    @GetMapping(value = "/dir/{containerId}")
    public ApiResponse listWorkspaceDirByContainerId(
        @PathVariable(value = "containerId") String containerId) {

        String workspaceDir = repository + "/workspace/" + containerId + "/data";

        List<TreeDTO> fileList = workspaceService.getFileInfoByPathContainChildren(workspaceDir);

        return ApiResponse.success(fileList);
    }

    /**
     * 根据containerId获取工作空间信息
     * @param containerId 容器id
     * @return org.opengms.admin.entity.dto.ApiResponse
     * @author bin
     **/
    @GetMapping(value = "/{containerId}")
    public ApiResponse getWorkspaceByContainerId(@PathVariable("containerId")Long containerId) {
        return ApiResponse.success(jupyterService.getJupyterContainerById(containerId));
    }



}
