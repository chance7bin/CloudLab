package org.opengms.admin.clients.container;

import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.dto.PageableParams;
import org.opengms.admin.entity.dto.container.InvokeDTO;
import org.opengms.admin.entity.dto.container.ModelService;
import org.opengms.admin.entity.dto.container.ModelServiceDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author 7bin
 * @date 2023/04/12
 */
@FeignClient(name = "container-service", url = "${labContainerUrl}")
public interface ModelServiceClient {

    String MODEL_SERVICE_MODULE = "/service";

    @PostMapping(value = MODEL_SERVICE_MODULE + "")
    ApiResponse insertModelService(@RequestBody ModelServiceDTO modelServiceDTO);


    @GetMapping(value = MODEL_SERVICE_MODULE + "/list")
    ApiResponse selectServiceList(@SpringQueryMap PageableParams pageableParams);

    @GetMapping(value = MODEL_SERVICE_MODULE + "/{msId}")
    ApiResponse getModelServiceById(@PathVariable("msId") String msId);

    @PostMapping(value = MODEL_SERVICE_MODULE + "/invoke")
    ApiResponse invoke(@RequestBody InvokeDTO invokeDTO);

}
