package org.opengms.admin.clients.container;

import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.dto.PageableParams;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author 7bin
 * @date 2023/04/12
 */
@FeignClient(name = "container-image", url = "${labContainerUrl}")
public interface ImageClient {

    String IMAGE_MODULE = "/image";

    @GetMapping(value = IMAGE_MODULE + "/list")
    ApiResponse listImages(@SpringQueryMap PageableParams pageableParams);



}
