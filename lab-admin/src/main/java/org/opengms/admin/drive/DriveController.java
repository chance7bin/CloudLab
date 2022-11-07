package org.opengms.admin.drive;

import cn.hutool.core.io.FileUtil;
import org.opengms.admin.annotation.Anonymous;
import org.opengms.admin.drive.entity.dto.FileDTO;
import org.opengms.admin.drive.entity.dto.TreeDTO;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.common.utils.file.FileTypeUtils;
import org.opengms.common.utils.file.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 云盘控制层
 *
 * @author 7bin
 * @date 2022/10/27
 */
@RestController
@RequestMapping("/drive")
public class DriveController {



    @Value(value = "${lab.repository.workspace}")
    private String workspace;

    @Autowired
    private IDriveService driveService;


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

    // @Anonymous
    @GetMapping(value = "/workspace/{containerName}")
    public ApiResponse listWorkspaceDirByContainerName(
        @PathVariable(value = "containerName") String containerName) {

        String workspaceDir = workspace + "/workspace/" + containerName;

        List<TreeDTO> fileList = driveService.getFileInfoByPathContainChildren(workspaceDir);

        return ApiResponse.success(fileList);
    }

}
