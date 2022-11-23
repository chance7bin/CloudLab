package org.opengms.admin.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.opengms.admin.clients.DriveClient;
import org.opengms.admin.entity.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件服务器控制层
 *
 * @author 7bin
 * @date 2022/11/21
 */
@RestController
@RequestMapping("/drive")
public class DriveController {

    @Autowired
    DriveClient driveClient;

    @GetMapping(value = "/workspace")
    public ApiResponse listWorkspaceDir(
        @RequestParam(value = "pathList", required = false) List<String> pathList) {

        return driveClient.listWorkspaceDir(pathList);

    }

    @GetMapping(value = "/workspace/{containerName}")
    public ApiResponse listWorkspaceDirByContainerName(
        @PathVariable(value = "containerName") String containerName) {

        return driveClient.listWorkspaceDirByContainerName(containerName);
    }

}
