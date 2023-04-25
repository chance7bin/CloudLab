package org.opengms.container.controller;

import org.opengms.container.controller.common.BaseController;
import org.opengms.container.entity.dto.ApiResponse;
import org.opengms.container.entity.dto.InvokeDTO;
import org.opengms.container.entity.dto.ModelServiceDTO;
import org.opengms.container.entity.dto.TableDataInfo;
import org.opengms.container.entity.po.ModelService;
import org.opengms.container.entity.vo.ModelServiceVO;
import org.opengms.container.service.IMSService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    public TableDataInfo selectServiceList(){
        startPage();
        List<ModelService> modelServices = msService.selectServiceList();
        TableDataInfo dataTable = getDataTable(modelServices);
        List<ModelServiceVO> res = modelServices.stream()
            .map(o -> {
                ModelServiceVO modelServiceVO = new ModelServiceVO();
                BeanUtils.copyProperties(o, modelServiceVO);
                return modelServiceVO;
            }).collect(Collectors.toList());
        dataTable.setRows(res);  // 如果转完再getDataTable，total会显示错误
        return dataTable;
    }

    @GetMapping(value = "/{msId}")
    public ApiResponse getModelServiceById(@PathVariable("msId") String msId){
        ModelService service = msService.getModelServiceById(msId);
        ModelServiceVO modelServiceVO = new ModelServiceVO();
        BeanUtils.copyProperties(service, modelServiceVO);
        return ApiResponse.success(modelServiceVO);
    }

    @PostMapping(value = "/invoke")
    public ApiResponse invoke(@RequestBody InvokeDTO invokeDTO){

        return ApiResponse.success("调用成功", msService.invoke(invokeDTO));
    }

}
