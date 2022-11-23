package org.opengms.container.controller;

import org.opengms.container.entity.dto.ApiResponse;
import org.opengms.container.service.IWorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作空间接口
 *
 * @author bin
 * @date 2022/10/6
 */
@RestController
@RequestMapping("/workspace")
public class WorkspaceController {

    @Autowired
    IWorkspaceService workspaceService;

    @GetMapping("/initialization/{username}/{imageName}")
    public ApiResponse initWorkspace(
        @PathVariable("username") String username,
        @PathVariable("imageName") String imageName){

        // Long userId = SecurityUtils.getUserId();
        return workspaceService.initWorkspace(username, imageName) ? ApiResponse.success() : ApiResponse.error();

    }


}
