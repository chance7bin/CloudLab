package org.opengms.container.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.opengms.common.utils.file.FileUtils;
import org.opengms.container.controller.common.BaseController;
import org.opengms.container.entity.dto.ApiResponse;
import org.opengms.container.entity.dto.TableDataInfo;
import org.opengms.container.entity.dto.docker.ImageInfoDTO;
import org.opengms.container.entity.po.docker.ImageInfo;
import org.opengms.container.service.IDockerService;
import org.opengms.container.service.IImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

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
        List<ImageInfo> imageInfos = imageService.listImages();
        TableDataInfo dataTable = getDataTable(imageInfos);
        List<ImageInfoDTO> dtoList = imageInfos.stream()
            .map(o -> {
                ImageInfoDTO imageInfoDTO = new ImageInfoDTO();
                imageInfoDTO.setImageId(o.getId());
                imageInfoDTO.setImageName(o.getImageName());
                imageInfoDTO.setTag(o.getTag());
                imageInfoDTO.setRepoTags(o.getRepoTags());
                imageInfoDTO.setSize(o.getSize() == null ? "unknown" : FileUtils.calcSize(o.getSize()));
                imageInfoDTO.setStatus(o.getStatus());
                return imageInfoDTO;
            })
            .collect(Collectors.toList());
        dataTable.setRows(dtoList);
        return dataTable;
    }


}
