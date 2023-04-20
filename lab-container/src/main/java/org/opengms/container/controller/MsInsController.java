package org.opengms.container.controller;

import org.opengms.container.entity.dto.ApiResponse;
import org.opengms.container.service.IMSInsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模型运行实例控制层
 *
 * @author 7bin
 * @date 2022/12/05
 */
@RestController
@RequestMapping("/instance")
public class MsInsController {

    @Autowired
    IMSInsService msInsService;


    @GetMapping(value = "/{msriId}")
    public ApiResponse getMsInsById(@PathVariable("msriId") String msriId){
        return ApiResponse.success(msInsService.getMsrInsByMsriId(msriId));
    }

}
