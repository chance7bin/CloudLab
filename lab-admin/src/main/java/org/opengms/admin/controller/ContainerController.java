package org.opengms.admin.controller;

import org.opengms.admin.annotation.Anonymous;
import org.opengms.admin.controller.common.BaseController;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.service.IDockerService;
import org.opengms.common.utils.http.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * docker容器控制层
 *
 * @author bin
 * @date 2022/09/20
 */
@RestController
@RequestMapping("/container")
public class ContainerController extends BaseController {

    @Autowired
    IDockerService dockerService;

    // @Anonymous
    @GetMapping(value = "/list/image")
    public ApiResponse listImages() {
        return ApiResponse.success(dockerService.listImages());
    }

    // @Anonymous
    @GetMapping(value = "/list/container")
    public ApiResponse listContainers() {
        return ApiResponse.success(dockerService.listContainers());
    }

    /**
     * 根据containerId获取容器信息
     * @param id
     * @return org.opengms.admin.entity.dto.ApiResponse
     * @author bin
     **/
    @GetMapping(value = "/jupyter/item/{id}")
    public ApiResponse getJupyterContainerById(@PathVariable("id")Long id) {
        return ApiResponse.success(dockerService.getJupyterContainerById(id));
    }


}
