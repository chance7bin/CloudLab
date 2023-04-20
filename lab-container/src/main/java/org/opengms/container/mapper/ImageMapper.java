package org.opengms.container.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.opengms.container.entity.po.docker.ImageInfo;

import java.util.List;

/**
 * docker 镜像 mapper
 *
 * @author 7bin
 * @date 2023/04/10
 */
@Mapper
public interface ImageMapper extends BaseMapper<ImageInfo>{

    /**
     * 根据镜像名获取同名镜像的数量
     * @param imageName 镜像名
     * @return {@link int}
     * @author 7bin
     **/
    int countImageByRepository(String imageName);

    /**
     * 根据镜像名+tag判断是否已经存在该镜像
     * @param repoTags 镜像名:tag 
     * @return {@link int} 
     * @author 7bin
     **/
    int isExistByRepoTags(String repoTags);

    ImageInfo getImageInfoByRepoTags(String repoTags);
}
