package org.opengms.container.clients;

import org.opengms.container.entity.dto.ApiResponse;
import org.opengms.container.entity.dto.drive.FileInfoDTO;
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


    @PostMapping("/file")
    ApiResponse addFile(@RequestBody FileInfoDTO fileInfoDTO);

    @GetMapping(value = "/file/info/{id}")
    ApiResponse getFileInfo(@PathVariable("id") Long id);
}
