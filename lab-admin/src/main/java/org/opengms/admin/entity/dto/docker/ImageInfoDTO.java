package org.opengms.admin.entity.dto.docker;

import lombok.Data;

/**
 * 镜像实体
 *
 * @author bin
 * @date 2022/10/12
 */
@Data
public class ImageInfoDTO {

    private String repoTags;

    /** 镜像大小 */
    private String size;

}
