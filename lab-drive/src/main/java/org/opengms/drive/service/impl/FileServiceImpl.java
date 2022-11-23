package org.opengms.drive.service.impl;

import cn.novelweb.tool.http.Result;
import cn.novelweb.tool.upload.local.LocalUpload;
import cn.novelweb.tool.upload.local.pojo.UploadFileParam;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.nacos.common.utils.UuidUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Results;
import org.opengms.common.utils.StringUtils;
import org.opengms.common.utils.file.FileTypeUtils;
import org.opengms.common.utils.uuid.SnowFlake;
import org.opengms.common.utils.uuid.UUID;
import org.opengms.drive.constant.FileConstants;
import org.opengms.drive.entity.po.FileInfo;
import org.opengms.drive.exception.ServiceException;
import org.opengms.drive.mapper.FileMapper;
import org.opengms.drive.service.IFileService;
import org.opengms.drive.utils.NovelWebUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author 7bin
 * @date 2022/11/22
 */
@Slf4j
@Service
public class FileServiceImpl implements IFileService {

    @Autowired
    private FileMapper fileMapper;

    @Value("${file.save-path}")
    private String savePath;
    @Value("${file.conf-path}")
    private String confFilePath;


    @Override
    public int uploadFiles(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        // 文件名非空校验
        if (StringUtils.isEmpty(fileName)) {
            throw new ServiceException("文件名不能为空");
        }
        // 大文件判定
        if (file.getSize() > FileConstants.MAX_SIZE) {
            throw new ServiceException("文件过大，请使用大文件传输");
        }
        // 生成新文件名
        String newName = UUID.fastUUID() + "_" + fileName;
        // 重命名文件
        File newFile = new File(savePath, fileName);
        // 如果该存储路径不存在则新建存储路径
        if (!newFile.getParentFile().exists()) {
            newFile.getParentFile().mkdirs();
        }
        // 文件写入
        try {
            file.transferTo(newFile);
        } catch (IOException e) {
            // e.printStackTrace();
            log.error("文件写入异常");
            throw new ServiceException("文件写入异常");
        }
        // 保存文件信息
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId(SnowFlake.nextId());
        fileInfo.setFileName(fileName);
        fileInfo.setFilePath(newName);
        fileInfo.setSuffix(FileTypeUtils.getFileType(fileName));
        fileMapper.insert(fileInfo);
        return fileMapper.insert(fileInfo);
    }

    @Override
    public InputStream getFileInputStream(Long id) {
        try {
            FileInfo fileInfo = fileMapper.selectById(id);
            File file = new File(savePath + File.separator + fileInfo.getFilePath());
            return new FileInputStream(file);
        } catch (Exception e) {
            log.error("获取文件输入流出错", e);
        }
        return null;
    }

    @Override
    public FileInfo getFileDetails(Long id) {
        return fileMapper.selectById(id);
    }

    @Override
    public List<FileInfo> getFileList(Integer pageNo, Integer pageSize) {

        return fileMapper.selectFileList();
    }

    @Override
    public Result<JSONArray> checkFileMd5(String md5, String fileName) {
        Result<JSONArray> result;
        try {
            String realFilename = md5 + "_" + fileName;
            result = LocalUpload.checkFileMd5(md5, realFilename, confFilePath, savePath);
        } catch (Exception e) {
            // e.printStackTrace();
            log.error(e.getMessage());
            throw new ServiceException(e.getMessage());
        }
        return result;

    }

    @Override
    public Result breakpointResumeUpload(UploadFileParam param, HttpServletRequest request) {
        Result result;
        try {
            // 这里的 chunkSize(分片大小) 要与前端传过来的大小一致
            // long chunkSize = Objects.isNull(param.getChunkSize()) ? 5 * 1024 * 1024
            //     : param.getChunkSize();

            // 实际存储的文件格式为 [{md5}_{filename}]
            String realFilename = param.getMd5() + "_" + param.getName();
            param.setName(realFilename);
            result = LocalUpload.fragmentFileUploader(param, confFilePath, savePath, 5242880L, request);
            // return NovelWebUtils.forReturn(result);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ServiceException(e.getMessage());
        }
        return result;
    }

    @Override
    public int addFile(FileInfo fileInfo) {
        fileInfo.setFileId(SnowFlake.nextId());
        fileInfo.setFilePath(fileInfo.getMd5() + "_" + fileInfo.getFileName());
        fileInfo.setSuffix(FileTypeUtils.getFileType(fileInfo.getFileName()));
        return fileMapper.insert(fileInfo);
    }
}
