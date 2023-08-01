package org.opengms.admin.clients;

import org.opengms.admin.constant.ServiceConstants;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.dto.drive.FileInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件服务器的远程调用
 *
 * @author 7bin
 * @date 2022/11/21
 */
// @FeignClient(name = "lab-drive", url = "${labDriveUrl}")
// 更换为nacos注册服务
@FeignClient(name = "lab-drive")
public interface DriveClient {

    @GetMapping(ServiceConstants.LAB_DRIVE_CONTEXT_PATH + "/workspace")
    ApiResponse listWorkspaceDir(@RequestParam(value = "pathList", required = false) List<String> pathList);


    @PostMapping(ServiceConstants.LAB_DRIVE_CONTEXT_PATH + "/file")
    ApiResponse addFile(@RequestBody FileInfoDTO fileInfoDTO);

    @GetMapping(value = ServiceConstants.LAB_DRIVE_CONTEXT_PATH + "/file/info/{id}")
    ApiResponse getFileInfo(@PathVariable("id") Long id);
}
