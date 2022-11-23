package org.opengms.admin.clients;

import org.opengms.admin.entity.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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

}
