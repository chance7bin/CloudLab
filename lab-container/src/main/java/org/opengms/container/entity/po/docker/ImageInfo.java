package org.opengms.container.entity.po.docker;

import lombok.Data;
import org.opengms.container.constant.ImageStatus;
import org.opengms.container.entity.BaseEntity;

/**
 * 镜像信息
 *
 * @author 7bin
 * @date 2022/12/15
 */
@Data
public class ImageInfo extends BaseEntity {

    /** 数据库中的镜像id */
    Long id;

    /** docker中的镜像id */
    String imageId;

    /** 镜像名 */
    String imageName;

    /** 镜像标签 */
    String tag;

    /** 镜像名:标签 */
    String repoTags;

    /** 镜像大小 */
    Long size;

    /** 镜像状态 */
    String status = ImageStatus.INIT;

    /** commit次数，不得超过127次 */
    int commitCount = 0;

    /** 是否删除 */
    boolean delFlag;


}
