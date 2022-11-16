package org.opengms.admin.msc.controller;

import org.opengms.admin.annotation.Anonymous;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.msc.controller.common.BaseController;
import org.opengms.admin.msc.entity.dto.ModelServiceDTO;
import org.opengms.admin.msc.entity.po.ModelService;
import org.opengms.admin.msc.service.IMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author 7bin
 * @date 2022/11/12
 */
@RestController
@RequestMapping("/msc/service")
public class ModelServiceController extends BaseController {

    @Autowired
    IMSService msService;

    @PostMapping(value = "")
    public ApiResponse insert(@RequestBody ModelServiceDTO modelServiceDTO){

        modelServiceDTO.setCreateBy(getUsername());
        int cnt = msService.insertModelService(modelServiceDTO);

        return affectRows(cnt);
    }


    @Anonymous
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

        msService.invoke(modelService);

        return ApiResponse.success();
    }

}
