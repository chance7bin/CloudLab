package org.opengms.admin.controller.container;

import org.opengms.admin.clients.ContainerClient;
import org.opengms.admin.controller.common.BaseController;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.dto.container.ModelService;
import org.opengms.admin.entity.dto.container.ModelServiceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author 7bin
 * @date 2022/11/12
 */
@RestController
@RequestMapping("/container/service")
public class ModelServiceController extends BaseController {

    @Autowired
    ContainerClient containerClient;

    @PostMapping(value = "")
    public ApiResponse insert(@RequestBody ModelServiceDTO modelServiceDTO){
        modelServiceDTO.setCreateBy(getUsername());
        return containerClient.insertModelService(modelServiceDTO);
    }


    @GetMapping(value = "/list")
    public ApiResponse selectServiceList(){
        return containerClient.selectServiceList();
    }

    @GetMapping(value = "/{msId}")
    public ApiResponse getModelServiceById(@PathVariable("msId") String msId){
        return containerClient.getModelServiceById(msId);
    }

    @PostMapping(value = "/invoke")
    public ApiResponse invoke(@RequestBody ModelService modelService){
        return containerClient.invoke(modelService);
    }

}
