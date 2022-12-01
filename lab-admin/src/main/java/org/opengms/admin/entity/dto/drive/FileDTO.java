package org.opengms.admin.entity.dto.drive;

import lombok.Data;

/**
 * 文件系统 DTO
 *
 * @author 7bin
 * @date 2022/11/29
 */
@Data
public class FileDTO {

    /** 请求的文件路径 */
    String dir;


    /** 路径id（文件id）*/
    Long pathId;

}
