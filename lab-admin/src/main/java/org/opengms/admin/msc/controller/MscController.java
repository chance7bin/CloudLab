package org.opengms.admin.msc.controller;

import org.opengms.admin.annotation.Anonymous;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.msc.controller.common.BaseController;
import org.opengms.admin.msc.service.IMSService;
import org.opengms.common.utils.TerminalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/** 模型服务容器控制层
 * @author 7bin
 * @date 2022/10/18
 */
@RestController
@RequestMapping("/msc")
public class MscController extends BaseController {

    @Autowired
    IMSService msService;

    @Anonymous
    @PostMapping(value = "/invoke")
    public ApiResponse invoke(){

        // msService.invoke();

        return ApiResponse.success();
    }


}
