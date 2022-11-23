package org.opengms.drive.service;

import cn.novelweb.tool.http.Result;
import cn.novelweb.tool.upload.local.pojo.UploadFileParam;
import com.alibaba.fastjson.JSONArray;
import org.opengms.drive.entity.po.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.List;

/**
 * @author 7bin
 * @date 2022/11/22
 */
public interface IFileService {

    /**
     * 小文件上传
     *
     * @param file 文件
     * @return {@link FileInfo}
     */
    int uploadFiles(MultipartFile file);

    /**
     * 获取文件输入流
     *
     * @param id id
     * @return {@link InputStream}
     */
    InputStream getFileInputStream(Long id);

    /**
     * 获取文件详情
     *
     * @param id
     * @return {@link FileInfo}
     * @author 7bin
     **/
    FileInfo getFileDetails(Long id);

    /**
     * 分页获取文件列表
     *
     * @param pageNo 页数
     * @param pageSize 页面大小
     * @return {@link List<FileInfo>}
     * @author 7bin
     **/
    List<FileInfo> getFileList(Integer pageNo, Integer pageSize);


    /**
     * 检查文件md5
     * @param md5 md5
     * @param fileName 文件名
     * @return {@link Result}
     * @author 7bin
     **/
    Result<JSONArray> checkFileMd5(String md5, String fileName);

    /**
     * 断点续传
     *
     * @param param   参数
     * @param request 请求
     * @return {@link Result}
     */
    Result breakpointResumeUpload(UploadFileParam param, HttpServletRequest request);

    /**
     * 添加文件
     *
     * @param fileInfo 文件信息
     * @return {@link int}
     * @author 7bin
     **/
    int addFile(FileInfo fileInfo);


}
