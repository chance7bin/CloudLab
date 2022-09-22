package org.opengms.admin.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.opengms.admin.controller.common.BaseController;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.dto.LoginDTO;
import org.opengms.admin.entity.dto.RegisterDTO;
import org.opengms.admin.entity.po.system.SysMenu;
import org.opengms.admin.entity.po.system.SysUser;
import org.opengms.admin.service.ISysConfigService;
import org.opengms.admin.service.ISysLoginService;
import org.opengms.admin.service.ISysMenuService;
import org.opengms.admin.service.ISysUserService;
import org.opengms.admin.utils.SecurityUtils;
import org.opengms.common.constant.Constants;
import org.opengms.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * 登录验证
 * 
 * @author bin
 * @date 2022/08/26
 */
@Api(tags = {"登录验证"})
@RestController
public class SysLoginController extends BaseController {

    @Autowired
    private ISysLoginService loginService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private ISysMenuService menuService;


    /**
     * 登录方法
     *
     * @param loginDTO 登录信息
     * @return 结果
     */
    @ApiImplicitParams({
        @ApiImplicitParam(paramType = "body", dataType = "LoginDTO", name = "loginDTO", value = "登录信息")
    })
    @ApiOperation(value = "登录方法", notes = "登录方法", httpMethod = "POST")
    @PostMapping("/login")
    public ApiResponse login(@RequestBody LoginDTO loginDTO)
    {
        ApiResponse apiResponse = ApiResponse.success();
        // 生成令牌
        String token = loginService.login(loginDTO.getUsername(), loginDTO.getPassword(), loginDTO.getCode(),
            loginDTO.getUuid());
        apiResponse.put(Constants.TOKEN, token);
        return apiResponse;
    }

    @ApiImplicitParams({
        @ApiImplicitParam(paramType = "body", dataType = "RegisterDTO", name = "user", value = "")
    })
    @ApiOperation(value = "注册方法", notes = "", httpMethod = "POST")
    @PostMapping("/register")
    public ApiResponse register(@RequestBody RegisterDTO user)
    {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser"))))
        {
            return error("当前系统没有开启注册功能！");
        }
        String msg = loginService.register(user);
        return StringUtils.isEmpty(msg) ? success() : error(msg);
    }


    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/getInfo")
    public ApiResponse getInfo()
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        // TODO: 2022/9/14 角色权限功能
        // 角色集合
        // Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        // Set<String> permissions = permissionService.getMenuPermission(user);
        ApiResponse ajax = ApiResponse.success();
        ajax.put("user", user);
        // ajax.put("roles", roles);
        // ajax.put("permissions", permissions);
        return ajax;
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("/getRouters")
    public ApiResponse getRouters()
    {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return ApiResponse.success(menuService.buildMenus(menus));
    }
    
}
