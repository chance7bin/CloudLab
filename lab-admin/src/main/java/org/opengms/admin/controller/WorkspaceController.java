package org.opengms.admin.controller;

import org.opengms.admin.annotation.Anonymous;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.service.IWorkspaceService;
import org.opengms.admin.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/initialization")
    public ApiResponse initWorkspace(){

        Long userId = SecurityUtils.getUserId();
        return workspaceService.initWorkspace(userId) ? ApiResponse.success() : ApiResponse.error();

    }


}
