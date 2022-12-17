package org.opengms.container.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.opengms.container.entity.dto.ApiResponse;
import org.opengms.container.entity.dto.workspace.TreeDTO;
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
@RestController
@RequestMapping("/workspace")
public class WorkspaceController {

    @Value(value = "${container.repository}")
    private String repository;

    @Autowired
    IWorkspaceService workspaceService;

    @GetMapping("/initialization/{username}/{imageName}/{containerName}")
    public ApiResponse initWorkspace(
        @PathVariable("username") String username,
        @PathVariable("imageName") String imageName,
        @PathVariable("containerName") String containerName){

        // Long userId = SecurityUtils.getUserId();
        return workspaceService.initWorkspace(username, imageName, containerName) ? ApiResponse.success() : ApiResponse.error();

    }


    @ApiImplicitParams({
        @ApiImplicitParam(paramType = "path", dataType = "string", dataTypeClass = String.class,name = "containerName", value = "", required = true)
    })
    @ApiOperation(value = "", notes = "", httpMethod = "GET")
    @GetMapping(value = "/dir/{containerId}")
    public ApiResponse listWorkspaceDirByContainerId(
        @PathVariable(value = "containerId") String containerId) {

        String workspaceDir = repository + "/workspace/" + containerId + "/data";

        List<TreeDTO> fileList = workspaceService.getFileInfoByPathContainChildren(workspaceDir);

        return ApiResponse.success(fileList);
    }


    /**
     * 根据containerId获取容器信息
     * @param id
     * @return org.opengms.admin.entity.dto.ApiResponse
     * @author bin
     **/
    @GetMapping(value = "/jupyter/item/{id}")
    public ApiResponse getJupyterContainerById(@PathVariable("id")Long id) {
        return ApiResponse.success(workspaceService.getJupyterContainerById(id));
    }



}
