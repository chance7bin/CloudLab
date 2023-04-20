package org.opengms.container.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.opengms.container.controller.common.BaseController;
import org.opengms.container.entity.dto.ApiResponse;
import org.opengms.container.entity.dto.TableDataInfo;
import org.opengms.container.entity.dto.docker.ImageInfoDTO;
import org.opengms.container.service.IDockerService;
import org.opengms.container.service.IImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * docker镜像 控制层
 *
 * @author 7bin
 * @date 2023/04/11
 */
@Api(value = "/image", tags = {"docker镜像"})
@RestController
@RequestMapping("/image")
public class ImageController extends BaseController {

    @Autowired
    IDockerService dockerService;

    @Autowired
    IImageService imageService;

    // @Anonymous
    @ApiOperation(value = "镜像列表", httpMethod = "GET")
    @GetMapping(value = "/list")
    public TableDataInfo listImages() {
        startPage();
        List<ImageInfoDTO> imageInfos = imageService.listImages();
        return getDataTable(imageInfos);
    }


}
