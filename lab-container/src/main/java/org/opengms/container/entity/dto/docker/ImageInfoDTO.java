package org.opengms.container.entity.dto.docker;

import lombok.Data;

/**
 * 镜像实体
 *
 * @author bin
 * @date 2022/10/12
 */
@Data
public class ImageInfoDTO {

    /** 镜像id */
    private Long imageId;

    /** 镜像名 */
    private String imageName;

    /** tag */
    private String tag;

    /** 镜像标签 */
    private String repoTags;

    /** 镜像大小 */
    private String size;

    /** 镜像状态 */
    private String status;

}
