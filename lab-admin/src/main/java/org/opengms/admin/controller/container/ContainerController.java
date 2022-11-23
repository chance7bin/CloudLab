package org.opengms.admin.controller.container;

import org.opengms.admin.clients.ContainerClient;
import org.opengms.admin.entity.dto.ApiResponse;
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
@RequestMapping("/container/container")
public class ContainerController{

    @Autowired
    ContainerClient containerClient;

    // @Anonymous
    @GetMapping(value = "/list/image")
    public ApiResponse listImages() {
        return containerClient.listImages();
    }

    // @Anonymous
    @GetMapping(value = "/list/container")
    public ApiResponse listContainers() {
        return containerClient.listContainers();
    }

    /**
     * 根据containerId获取容器信息
     * @param id
     * @return org.opengms.admin.entity.dto.ApiResponse
     * @author bin
     **/
    @GetMapping(value = "/jupyter/item/{id}")
    public ApiResponse getJupyterContainerById(@PathVariable("id")Long id) {
        return containerClient.getJupyterContainerById(id);
    }


}
