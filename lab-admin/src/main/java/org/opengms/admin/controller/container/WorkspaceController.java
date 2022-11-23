package org.opengms.admin.controller.container;

import org.opengms.admin.clients.ContainerClient;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.po.system.SysUser;
import org.opengms.admin.service.ISysUserService;
import org.opengms.admin.utils.SecurityUtils;
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
@RequestMapping("/container/workspace")
public class WorkspaceController {

    @Autowired
    ContainerClient containerClient;

    @Autowired
    ISysUserService sysUserService;

    @GetMapping("/initialization/{imageName}")
    public ApiResponse initWorkspace(@PathVariable("imageName") String imageName){

        Long userId = SecurityUtils.getUserId();
        SysUser sysUser = sysUserService.selectUserById(userId);
        return containerClient.initWorkspace(sysUser.getUserName(), imageName);

    }


}
