package org.opengms.drive.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.SecureUtil;
import cn.novelweb.tool.http.Result;
import cn.novelweb.tool.upload.local.LocalUpload;
import cn.novelweb.tool.upload.local.pojo.UploadFileParam;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.opengms.common.utils.DateUtils;
import org.opengms.common.utils.StringUtils;
import org.opengms.common.utils.file.FileTypeUtils;
import org.opengms.common.utils.uuid.SnowFlake;
import org.opengms.common.utils.uuid.UUID;
import org.opengms.drive.constant.FileConstants;
import org.opengms.drive.entity.po.FileInfo;
import org.opengms.drive.exception.ServiceException;
import org.opengms.drive.mapper.FileMapper;
import org.opengms.drive.service.IFileService;
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
    public Long uploadFiles(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        // 文件名非空校验
        if (StringUtils.isEmpty(fileName)) {
            throw new ServiceException("文件名不能为空");
        }
        // 大文件判定
        if (file.getSize() > FileConstants.MAX_SIZE) {
            // throw new ServiceException("文件过大，请使用大文件传输");
        }
        // 生成新文件名
        String newName = UUID.fastUUID() + "_" + fileName;

        int year = DateUtils.getYear();
        int month = DateUtils.getMonth();
        String separator = "/";
        // 重命名文件
        File newFile = new File(savePath + separator + year + separator + month + separator, newName);
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
        fileInfo.setFilePath(year + separator + month + separator + newName);
        fileInfo.setMd5(SecureUtil.md5(newFile));
        fileInfo.setSize(String.valueOf(FileUtil.size(newFile)));
        fileInfo.setSuffix(FileTypeUtils.getFileType(fileName));
        int insert = fileMapper.insert(fileInfo);
        if (insert > 0){
            return fileInfo.getFileId();
        } else {
            throw new ServiceException("插入数据失败");
        }
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

        boolean exist = fileMapper.fileIsExist(md5);
        if (exist){
            return null;
        }

        Result<JSONArray> result;
        try {
            // String realFilename = md5 + "_" + fileName;
            String realFilename = md5;
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
            // String realFilename = param.getMd5() + "_" + param.getName();
            String realFilename = param.getMd5();
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
    public FileInfo addFile(FileInfo fileInfo) {

        fileInfo.setFileId(SnowFlake.nextId());

        boolean exist = fileMapper.fileIsExist(fileInfo.getMd5());
        if (exist){
            // 如果数据库中已经存在该文件的话
            // filePath与数据库已存在的文件路径相同
            FileInfo fileInfo1 = fileMapper.selectFirstByMd5(fileInfo.getMd5());
            fileInfo.setFilePath(fileInfo1.getFilePath());
            fileInfo.setSuffix(fileInfo1.getSuffix());

        } else {
            // 如果数据库不存在该文件, 说明是第一次上传该文件
            // 1. 移动该文件及该文件的conf 移动到 year/month文件夹中
            // 2. 将该文件的信息入库

            String separator = "/";
            int year = DateUtils.getYear();
            int month = DateUtils.getMonth();
            // String name = fileInfo.getMd5() + "_" + fileInfo.getFileName();
            String name = fileInfo.getMd5();
            FileUtil.move(
                new File(savePath + separator + name),
                new File(savePath + separator + year + separator + month + separator + name),
                false
            );
            FileUtil.move(
                new File(confFilePath + separator + name + ".conf"),
                new File(savePath + separator + year + separator + month + separator + "conf" + separator + name + ".conf"),
                false
            );

            fileInfo.setFilePath(year + separator + month + separator + name);
            if (StringUtils.isEmpty(fileInfo.getSuffix())){
                File file = new File(savePath + separator + fileInfo.getFilePath());
                fileInfo.setSuffix(FileTypeUtils.getFileType(fileInfo.getFileName()));
            }
        }

        int cnt = fileMapper.insert(fileInfo);
        return  cnt > 0 ? fileInfo : null;

        // fileInfo.setFileId(SnowFlake.nextId());
        // fileInfo.setFilePath(fileInfo.getMd5() + "_" + fileInfo.getFileName());
        // if (StringUtils.isEmpty(fileInfo.getSuffix())){
        //     File file = new File(savePath + separator + fileInfo.getFilePath());
        //     fileInfo.setSuffix(FileNameUtil.extName(file));
        // }
        // int cnt = fileMapper.insert(fileInfo);
        // return  cnt > 0 ? fileInfo : null;
    }
}
