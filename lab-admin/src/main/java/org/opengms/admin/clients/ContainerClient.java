package org.opengms.admin.clients;

import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.dto.container.ModelService;
import org.opengms.admin.entity.dto.container.ModelServiceDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

/**
 * 模型容器的远程调用
 *
 * @author 7bin
 * @date 2022/11/18
 */
@FeignClient(name = "lab-container", url = "${labContainerUrl}")
public interface ContainerClient {

    String CONTAINER_MODULE = "/container";
    String WORKSPACE_MODULE = "/workspace";
    String MODEL_SERVICE_MODULE = "/service";

    @GetMapping(value = CONTAINER_MODULE + "/list/image")
    public ApiResponse listImages();

    @GetMapping(value = CONTAINER_MODULE + "/list/container")
    ApiResponse listContainers();


    @GetMapping(value = CONTAINER_MODULE + "/jupyter/item/{id}")
    ApiResponse getJupyterContainerById(@PathVariable("id")Long id);



    @GetMapping(WORKSPACE_MODULE + "/initialization/{username}/{imageName}")
    ApiResponse initWorkspace(@PathVariable("username") String username,
        @PathVariable("imageName") String imageName);


    @PostMapping(value = MODEL_SERVICE_MODULE + "")
    ApiResponse insertModelService(@RequestBody ModelServiceDTO modelServiceDTO);


    @GetMapping(value = MODEL_SERVICE_MODULE + "/list")
    ApiResponse selectServiceList();

    @GetMapping(value = MODEL_SERVICE_MODULE + "/{msId}")
    ApiResponse getModelServiceById(@PathVariable("msId") String msId);

    @PostMapping(value = MODEL_SERVICE_MODULE + "/invoke")
    ApiResponse invoke(@RequestBody ModelService modelService);
}
