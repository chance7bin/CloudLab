package org.opengms.admin.clients.container;

import org.opengms.admin.constant.ServiceConstants;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.dto.PageableParams;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author 7bin
 * @date 2023/04/12
 */
// @FeignClient(name = "container-instance", url = "${labContainerUrl}")
@FeignClient(name = "lab-container", contextId = "instance")
public interface MsInsClient {

    String INSTANCE_MODULE = ServiceConstants.LAB_CONTAINER_CONTEXT_PATH + "/instance";

    @GetMapping(value = INSTANCE_MODULE + "/{msriId}")
    ApiResponse getMsInsById(@PathVariable("msriId") String msriId);

    @GetMapping(value = INSTANCE_MODULE + "/list")
    ApiResponse getMsInsList(@SpringQueryMap PageableParams pageableParams);

}
