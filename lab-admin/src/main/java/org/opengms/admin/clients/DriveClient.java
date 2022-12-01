package org.opengms.admin.clients;

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
@FeignClient(name = "lab-drive", url = "${labDriveUrl}")
public interface DriveClient {

    @GetMapping("/workspace")
    ApiResponse listWorkspaceDir(@RequestParam(value = "pathList", required = false) List<String> pathList);

    @GetMapping(value = "/workspace/{containerName}")
    ApiResponse listWorkspaceDirByContainerName(@PathVariable(value = "containerName") String containerName);


    @PostMapping("/file")
    ApiResponse addFile(@RequestBody FileInfoDTO fileInfoDTO);

}
