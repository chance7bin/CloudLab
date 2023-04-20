package org.opengms.admin.controller.container;

import org.opengms.admin.clients.container.ContainerClient;
import org.opengms.admin.clients.container.ImageClient;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.dto.PageableParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * docker容器控制层
 *
 * @author bin
 * @date 2022/09/20
 */
@RestController
@RequestMapping("/container/image")
public class ImageController {

    @Autowired
    ImageClient imageClient;

    // @Anonymous
    @GetMapping(value = "/list")
    public ApiResponse listImages(@SpringQueryMap PageableParams pageableParams) {
        return imageClient.listImages(pageableParams);
    }


}
