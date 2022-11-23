package org.opengms.drive.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.opengms.drive.service.IDriveService;
import org.opengms.drive.entity.dto.ApiResponse;
import org.opengms.drive.entity.dto.FileDTO;
import org.opengms.drive.entity.dto.TreeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 云盘控制层
 *
 * @author 7bin
 * @date 2022/10/27
 */
@RestController
public class DriveController {



    @Value(value = "${drive.repository.workspace}")
    private String workspace;

    @Autowired
    private IDriveService driveService;


    @ApiImplicitParams({
        @ApiImplicitParam(paramType = "query", dataType = "List<String>", dataTypeClass = String.class, name = "pathList", value = "")
    })
    @ApiOperation(value = "", notes = "", httpMethod = "GET")
    @GetMapping(value = "/workspace")
    public ApiResponse listWorkspaceDir(
        @RequestParam(value = "pathList", required = false) List<String> pathList) {

        String workspaceDir = workspace + "/workspace";

        StringBuilder queryDir = new StringBuilder(workspaceDir);

        if (pathList != null){
            for (String path : pathList) {
                queryDir.append("/").append(path);
            }
        }

        List<FileDTO> fileDTOList = driveService.getFileInfoByPath(queryDir.toString());

        return ApiResponse.success(fileDTOList);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(paramType = "path", dataType = "string", dataTypeClass = String.class,name = "containerName", value = "", required = true)
    })
    @ApiOperation(value = "", notes = "", httpMethod = "GET")
    @GetMapping(value = "/workspace/{containerName}")
    public ApiResponse listWorkspaceDirByContainerName(
        @PathVariable(value = "containerName") String containerName) {

        String workspaceDir = workspace + "/workspace/" + containerName;

        List<TreeDTO> fileList = driveService.getFileInfoByPathContainChildren(workspaceDir);

        return ApiResponse.success(fileList);
    }

}
