package org.opengms.admin.controller.container;

import org.opengms.admin.clients.container.ContainerClient;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.dto.PageableParams;
import org.opengms.admin.entity.dto.container.EnvDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping(value = "/list")
    public ApiResponse listContainers(@SpringQueryMap PageableParams pageableParams) {
        return containerClient.listContainers(pageableParams);
    }

    @PostMapping(value = "/newEnv")
    public ApiResponse createNewEnv(@RequestBody EnvDTO envDTO){
        return containerClient.createNewEnv(envDTO);
    }



}
