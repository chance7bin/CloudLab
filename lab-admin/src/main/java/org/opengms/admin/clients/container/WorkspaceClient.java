package org.opengms.admin.clients.container;

import org.opengms.admin.entity.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author 7bin
 * @date 2023/04/12
 */
@FeignClient(name = "container-workspace", url = "${labContainerUrl}")
public interface WorkspaceClient {

    String WORKSPACE_MODULE = "/workspace";

    @GetMapping(WORKSPACE_MODULE + "/initialization/{username}/{imageId}/{containerName}")
    ApiResponse initWorkspace(
        @PathVariable("username") String username,
        @PathVariable("imageId") String imageId,
        @PathVariable("containerName") String containerName);

    @GetMapping(value = WORKSPACE_MODULE + "/dir/{containerId}")
    ApiResponse listWorkspaceDirByContainerId(@PathVariable(value = "containerId") String containerId);


    @GetMapping(value = WORKSPACE_MODULE + "/{containerId}")
    ApiResponse getWorkspaceByContainerId(@PathVariable("containerId")Long containerId);
}
