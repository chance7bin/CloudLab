package org.opengms.admin.controller;

import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.po.system.SysUser;
import org.opengms.admin.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 用户接口
 * @author bin
 * @date 2022/08/22
 */
@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private ISysUserService userService;

    @GetMapping(value = "/{userId}" )
    public SysUser selectUserById(@PathVariable(value = "userId") Long userId){
        // Long userId = 101L;
        return userService.selectUserById(userId);
    }

    @GetMapping(value = "/auth" )
    public ApiResponse auth(){
        // Long userId = 101L;
        return ApiResponse.success();
    }


}
