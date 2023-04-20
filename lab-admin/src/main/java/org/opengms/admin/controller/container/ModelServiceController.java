package org.opengms.admin.controller.container;

import org.opengms.admin.clients.container.ModelServiceClient;
import org.opengms.admin.controller.common.BaseController;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.dto.PageableParams;
import org.opengms.admin.entity.dto.container.InvokeDTO;
import org.opengms.admin.entity.dto.container.ModelService;
import org.opengms.admin.entity.dto.container.ModelServiceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

/**
 * @author 7bin
 * @date 2022/11/12
 */
@RestController
@RequestMapping("/container/service")
public class ModelServiceController extends BaseController {

    @Autowired
    ModelServiceClient modelServiceClient;

    @PostMapping(value = "")
    public ApiResponse insert(@RequestBody ModelServiceDTO modelServiceDTO){
        modelServiceDTO.setCreateBy(getUsername());
        return modelServiceClient.insertModelService(modelServiceDTO);
    }


    @GetMapping(value = "/list")
    public ApiResponse selectServiceList(@SpringQueryMap PageableParams pageableParams){
        return modelServiceClient.selectServiceList(pageableParams);
    }

    @GetMapping(value = "/{msId}")
    public ApiResponse getModelServiceById(@PathVariable("msId") String msId){
        return modelServiceClient.getModelServiceById(msId);
    }

    @PostMapping(value = "/invoke")
    public ApiResponse invoke(@RequestBody InvokeDTO invokeDTO){
        return modelServiceClient.invoke(invokeDTO);
    }

}
