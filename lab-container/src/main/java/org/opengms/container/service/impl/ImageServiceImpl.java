package org.opengms.container.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.opengms.common.utils.StringUtils;
import org.opengms.common.utils.file.FileUtils;
import org.opengms.container.entity.dto.docker.ImageInfoDTO;
import org.opengms.container.entity.po.docker.ImageInfo;
import org.opengms.container.mapper.ImageMapper;
import org.opengms.container.service.IImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 7bin
 * @date 2023/04/11
 */
@Service
@Slf4j
public class ImageServiceImpl implements IImageService {

    @Autowired
    ImageMapper imageMapper;

    @Override
    public List<ImageInfo> listImages() {
        List<ImageInfo> imageInfos = imageMapper.selectList();

        return imageInfos;
    }

    @Override
    public int insert(ImageInfo image) {
        return imageMapper.insert(image);
    }
}
