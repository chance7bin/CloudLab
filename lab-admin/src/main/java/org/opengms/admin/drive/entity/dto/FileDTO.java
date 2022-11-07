package org.opengms.admin.drive.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * 文件传输
 *
 * @author 7bin
 * @date 2022/10/27
 */
@Data
public class FileDTO {

    String filename;

    String fileType;

    String fileSize;

    Boolean directory;

    List<FileDTO> fileList;

}
