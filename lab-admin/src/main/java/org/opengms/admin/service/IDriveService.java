package org.opengms.admin.service;

import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.po.drive.FileInfo;

import java.util.List;

/**
 * @author 7bin
 * @date 2022/11/29
 */
public interface IDriveService {


    List<FileInfo> getFileList(String pathId, String username);

    int addFile(FileInfo fileInfo);
}
