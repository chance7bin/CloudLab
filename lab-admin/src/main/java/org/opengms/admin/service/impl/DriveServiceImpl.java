package org.opengms.admin.service.impl;

import cn.hutool.core.io.file.FileNameUtil;
import com.alibaba.fastjson2.JSONObject;
import org.apache.ibatis.annotations.Param;
import org.opengms.admin.clients.DriveClient;
import org.opengms.admin.constant.HttpStatus;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.dto.drive.FileInfoDTO;
import org.opengms.admin.entity.po.drive.FileInfo;
import org.opengms.admin.exception.ServiceException;
import org.opengms.admin.mapper.DriveMapper;
import org.opengms.admin.service.IDriveService;
import org.opengms.common.utils.file.FileTypeUtils;
import org.opengms.common.utils.uuid.SnowFlake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * @author 7bin
 * @date 2022/11/29
 */
@Service
public class DriveServiceImpl implements IDriveService {

    @Autowired
    DriveMapper driveMapper;

    @Autowired
    DriveClient driveClient;


    @Override
    public List<FileInfo> getFileList(String parentId, String username) {

        List<FileInfo> fileInfos = driveMapper.selectFileByUser(parentId, username);
        return fileInfos;

    }

    @Override
    public int addFile(FileInfo fileInfo) {

        FileInfo file = existFileInPath(fileInfo.getFilename(), fileInfo.getParentId());
        if (file != null && file.getDirectory()){
            if (fileInfo.getDirectory()){
                throw new ServiceException("此目标已包含名为" + fileInfo.getFilename() + "的文件或文件夹");
            } else {
                throw new ServiceException("指定的文件名与已存在的文件或文件夹重名，请指定其他名称");
            }
        }

        if (fileInfo.getDirectory()){
            //添加的是文件夹的话走这里
            fileInfo.setFileId(SnowFlake.nextId());
            return driveMapper.insert(fileInfo);
        } else {
            //添加的是文件的话走这里
            FileInfoDTO fileInfoDTO = new FileInfoDTO();
            fileInfoDTO.setFileName(fileInfo.getFilename());
            fileInfoDTO.setMd5(fileInfo.getMd5());
            fileInfoDTO.setSize(fileInfo.getSize());
            // fileInfoDTO.setSuffix(fileInfo.getType());
            ApiResponse result = driveClient.addFile(fileInfoDTO);
            if ((Integer) result.get(ApiResponse.CODE_TAG) == HttpStatus.SUCCESS){
                HashMap<String, Object> data = (HashMap<String, Object>) result.get(ApiResponse.DATA_TAG);
                fileInfo.setFileId(SnowFlake.nextId());
                // fileInfo.setType((String) data.getOrDefault("suffix", null));
                fileInfo.setType(FileTypeUtils.getFileType(fileInfo.getFilename()));
                fileInfo.setDriveFileId((String) data.get("fileId"));

                int cnt = 1;
                String filename = fileInfo.getFilename();
                String oriFilename = fileInfo.getFilename();
                String extName = fileInfo.getType() == null ? FileTypeUtils.getFileType(oriFilename) : fileInfo.getType();
                // 判断是否有同名的，有的话在文件后面 + (1) 命名
                while (existFileInPath(filename ,fileInfo.getParentId()) != null){
                    if (extName == null || "".equals(extName) || oriFilename.lastIndexOf(extName) == -1){
                        filename = oriFilename + " (" + cnt + ")";
                    } else {
                        int extNameIndex = oriFilename.lastIndexOf(extName);
                        String mainName = oriFilename.substring(0, extNameIndex - 1);
                        filename = mainName + " (" + cnt + ")" + "." + extName;
                    }
                    cnt++;
                }
                fileInfo.setFilename(filename);
                return driveMapper.insert(fileInfo);
            } else {
                //文件信息上传失败
                return 0;
            }
        }

    }

    // private Boolean existFileInPath(String filename, String parentId){
    //     if (driveMapper.existFileInPath(filename, parentId) > 0){
    //         return true;
    //     } else {
    //         return false;
    //     }
    // }

    // 判断在parentId路径下是否有相同文件名的文件
    private FileInfo existFileInPath(String filename, String parentId){
        return driveMapper.existFileInPath(filename, parentId);
    }

}
