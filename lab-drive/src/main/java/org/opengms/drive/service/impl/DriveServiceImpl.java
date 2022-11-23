package org.opengms.drive.service.impl;

import cn.hutool.core.io.FileUtil;
import org.opengms.drive.entity.dto.FileDTO;
import org.opengms.drive.entity.dto.TreeDTO;
import org.opengms.common.utils.file.FileTypeUtils;
import org.opengms.common.utils.file.FileUtils;
import org.opengms.drive.service.IDriveService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 7bin
 * @date 2022/11/01
 */
@Service
public class DriveServiceImpl implements IDriveService {


    @Override
    public List<FileDTO> getFileInfoByPath(String path) {
        List<File> files = FileUtils.ls(path);
        List<FileDTO> fileDTOList = new ArrayList<>();
        for (File file : files) {
            FileDTO fileDTO = new FileDTO();
            fileDTO.setFilename(file.getName());
            fileDTO.setDirectory(file.isDirectory());
            if (!file.isDirectory()){
                fileDTO.setFileType(FileTypeUtils.getFileType(file));
                fileDTO.setFileSize(FileUtils.calcSize(FileUtil.size(file)));
            }
            fileDTOList.add(fileDTO);
        }

        return fileDTOList;
    }

    @Override
    public List<TreeDTO> getFileInfoByPathContainChildren(String path){
        return getFileInfoByPathContainChildren(path, path);
    }

    public List<TreeDTO> getFileInfoByPathContainChildren(String path, String rootPath){
        if (rootPath == null){
            rootPath = path;
        }

        List<File> files = FileUtils.ls(path);
        List<TreeDTO> fileDTOList = new ArrayList<>();
        for (File file : files) {
            TreeDTO fileDTO = new TreeDTO();
            fileDTO.setLabel(file.getName());
            fileDTO.setRelativePath(FileUtils.getFileRelativePath(file,rootPath));
            // fileDTO.setDirectory(file.isDirectory());
            if (!file.isDirectory()){
                // fileDTO.setFileType(FileTypeUtils.getFileType(file));
                // fileDTO.setFileSize(FileUtils.calcSize(FileUtil.size(file)));
            } else {
                fileDTO.setChildren(getFileInfoByPathContainChildren(FileUtil.getCanonicalPath(file), rootPath));
            }
            fileDTOList.add(fileDTO);

        }
        return fileDTOList;
    }
}
