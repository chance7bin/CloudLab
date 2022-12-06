package org.opengms.admin.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.opengms.admin.clients.DriveClient;
import org.opengms.admin.controller.common.BaseController;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.po.drive.FileInfo;
import org.opengms.admin.service.IDriveService;
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
public class DriveController extends BaseController {

    @Autowired
    IDriveService driveService;

    @Autowired
    DriveClient driveClient;

    @GetMapping(value = "/workspace")
    public ApiResponse listWorkspaceDir(
        @RequestParam(value = "pathList", required = false) List<String> pathList) {

        return driveClient.listWorkspaceDir(pathList);

    }




    @GetMapping(value = "/list")
    public ApiResponse getFileList(@RequestParam("parentId") String parentId) {
        String username = getUsername();
        return ApiResponse.success(driveService.getFileList(parentId, username));

    }

    @PostMapping(value = "/file")
    public ApiResponse addFile(@RequestBody FileInfo fileInfo){
        fileInfo.setCreateBy(getUsername());
        return driveService.addFile(fileInfo) > 0 ? ApiResponse.success() : ApiResponse.error();
    }

}
