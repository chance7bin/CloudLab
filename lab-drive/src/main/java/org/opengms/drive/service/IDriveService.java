package org.opengms.drive.service;

import org.opengms.drive.entity.dto.FileDTO;
import org.opengms.drive.entity.dto.TreeDTO;

import java.util.List;

/**
 * @author 7bin
 * @date 2022/11/01
 */
public interface IDriveService {

    List<FileDTO> getFileInfoByPath(String path);

    List<TreeDTO> getFileInfoByPathContainChildren(String path);

}
