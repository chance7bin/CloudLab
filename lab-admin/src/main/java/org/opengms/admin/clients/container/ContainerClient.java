package org.opengms.admin.clients.container;

import org.opengms.admin.constant.ServiceConstants;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.dto.PageableParams;
import org.opengms.admin.entity.dto.container.EnvDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 模型容器的远程调用
 *
 * @author 7bin
 * @date 2023/04/12
 */
// 更换为nacos注册服务
// @FeignClient(name = "container-container", url = "${labContainerUrl}")
@FeignClient(name = "lab-container", contextId = "container")
public interface ContainerClient {


    String CONTAINER_MODULE = ServiceConstants.LAB_CONTAINER_CONTEXT_PATH + "/container";

    @GetMapping(value = CONTAINER_MODULE + "/list")
    ApiResponse listContainers(@SpringQueryMap PageableParams pageableParams);


    @PostMapping(value = CONTAINER_MODULE + "/newEnv")
    ApiResponse createNewEnv(@RequestBody EnvDTO envDTO);

}
