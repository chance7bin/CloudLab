package org.opengms.container.service;

import org.opengms.container.entity.dto.docker.ImageInfoDTO;
import org.opengms.container.entity.po.docker.ImageInfo;

import java.util.List;

/**
 * 镜像 服务层
 *
 * @author 7bin
 * @date 2023/04/11
 */
public interface IImageService {

    List<ImageInfo> listImages();

    int insert(ImageInfo image);

}
