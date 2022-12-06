package org.opengms.container.controller;

import org.opengms.container.controller.common.BaseController;
import org.opengms.container.entity.dto.ApiResponse;
import org.opengms.container.entity.dto.ModelServiceDTO;
import org.opengms.container.entity.po.ModelService;
import org.opengms.container.service.IMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author 7bin
 * @date 2022/11/12
 */
@RestController
@RequestMapping("/service")
public class ModelServiceController extends BaseController {

    @Autowired
    IMSService msService;

    @PostMapping(value = "")
    public ApiResponse insert(@RequestBody ModelServiceDTO modelServiceDTO){

        // modelServiceDTO.setCreateBy(getUsername());
        int cnt = msService.insertModelService(modelServiceDTO);

        return affectRows(cnt);
    }


    @GetMapping(value = "/list")
    public ApiResponse selectServiceList(){
        return ApiResponse.success(msService.selectServiceList());
    }

    @GetMapping(value = "/{msId}")
    public ApiResponse getModelServiceById(@PathVariable("msId") String msId){
        return ApiResponse.success(msService.getModelServiceById(msId));
    }

    @PostMapping(value = "/invoke")
    public ApiResponse invoke(@RequestBody ModelService modelService){

        return ApiResponse.success("调用成功", msService.invoke(modelService));
    }

}
