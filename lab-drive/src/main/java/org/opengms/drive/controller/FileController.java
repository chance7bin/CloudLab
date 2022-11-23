package org.opengms.drive.controller;

import cn.novelweb.tool.http.Result;
import cn.novelweb.tool.upload.local.pojo.UploadFileParam;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.omg.CORBA.SystemException;
import org.opengms.common.utils.StringUtils;
import org.opengms.drive.constant.FileConstants;
import org.opengms.drive.entity.dto.ApiResponse;
import org.opengms.drive.entity.dto.Chunk;
import org.opengms.drive.entity.po.FileInfo;
import org.opengms.drive.exception.ServiceException;
import org.opengms.drive.service.IFileService;
import org.opengms.drive.utils.EncodingUtils;
import org.opengms.drive.utils.InputStreamUtils;
import org.opengms.drive.utils.NovelWebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author 7bin
 * @date 2022/11/22
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    IFileService fileService;

    /**
     * 获取文件列表
     *
     * @param pageNo   当前页
     * @param pageSize 分页大小
     * @return {@link ApiResponse}
     * @author 7bin
     **/
    @GetMapping(value = "/list")
    public ApiResponse getFileList(@RequestParam Integer pageNo, @RequestParam Integer pageSize) {
        return ApiResponse.success(fileService.getFileList(pageNo, pageSize));
    }

    /**
     * 普通上传方式上传文件：用于小文件的上传，等待时间短，不会产生配置数据
     *
     * @param file 文件
     * @return {@link ApiResponse}
     * @author 7bin
     **/
    @PostMapping("/upload")
    public ApiResponse uploadFiles(MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("文件不能为空");
        }
        return fileService.uploadFiles(file) > 0 ? ApiResponse.success() : ApiResponse.error();
    }

    /**
     * 断点续传完成后上传文件信息进行入库操作
     *
     * @param fileInfo
     * @return {@link ApiResponse}
     * @author 7bin
     **/
    // @PostMapping("/add")
    @PostMapping("")
    public ApiResponse addFile(@RequestBody FileInfo fileInfo) {
        return fileService.addFile(fileInfo) > 0 ? ApiResponse.success() : ApiResponse.error();
    }

    /**
     * 检查文件MD5（文件MD5若已存在进行秒传）
     *
     * @param md5      md5
     * @param fileName 文件名称
     * @return {@link ApiResponse}
     * @author 7bin
     **/
    @GetMapping(value = "/check")
    public ApiResponse checkFileMd5(String md5, String fileName) {
        Result result = fileService.checkFileMd5(md5, fileName);
        return NovelWebUtils.forReturn(result);
    }

    /**
     * 断点续传方式上传文件：用于大文件上传
     *
     * @param chunkDTO   参数
     * @param request 请求
     * @return {@link ApiResponse}
     * @author 7bin
     **/
    @PostMapping(value = "/breakpoint-upload", consumes = "multipart/*", headers = "content-type=multipart/form-data", produces = "application/json;charset=UTF-8")
    public ApiResponse breakpointResumeUpload(Chunk chunkDTO, HttpServletRequest request) {

        String id = chunkDTO.getIdentifier();
        int chunks = Math.toIntExact(chunkDTO.getTotalChunks());
        int chunk = chunkDTO.getChunkNumber() - 1;
        long size = chunkDTO.getCurrentChunkSize();
        String name = chunkDTO.getFilename();
        MultipartFile file = chunkDTO.getFile();
        String md5 = chunkDTO.getIdentifier();
        UploadFileParam param = new UploadFileParam(id, chunks, chunk, size, name, file, md5);

        // return ApiResponse.success();
        Result result = fileService.breakpointResumeUpload(param, request);
        return NovelWebUtils.forReturn(result);
    }

    @GetMapping(value = "/breakpoint-upload")
    public ApiResponse breakpointResumeUploadPre(
        @RequestParam Map<String, String> chunkMap) {

        String md5 = chunkMap.get("identifier");
        String filename = chunkMap.get("filename");
        Result<JSONArray> result = fileService.checkFileMd5(md5, filename);

        JSONObject res = new JSONObject();

        boolean skipUpload = false;
        if ("200".equals(result.getCode()) || "201".equals(result.getCode())) {
            skipUpload = true;
        } else if ("206".equals(result.getCode())) {
            // 已经上传部分分块
            // data中存放的是还未上传的分块
            JSONArray data = result.getData();
            res.put("missChunks",data);
        }

        res.put("skipUpload",skipUpload);

        return ApiResponse.success(res);
        // Result result = fileService.breakpointResumeUpload(param, request);
        // return NovelWebUtils.forReturn(result);
    }

    /**
     * 图片/PDF查看
     *
     * @param id id
     * @return {@link ResponseEntity}<{@link byte[]}>
     */
    @GetMapping(value = "/view/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> viewFilesImage(@PathVariable Long id) throws IOException {
        FileInfo fileDetails = fileService.getFileDetails(id);
        if (fileDetails == null){
            throw new ServiceException("未找到该文件");
        }
        if (!FileConstants.IMAGE_TYPE.contains(fileDetails.getSuffix())){
            throw new ServiceException("非图片/PDF类型请先下载");
        }
        InputStream inputStream = fileService.getFileInputStream(id);
        return new ResponseEntity<>(InputStreamUtils.inputStreamToByte(inputStream), HttpStatus.OK);
    }

    /**
     * 文件下载
     *
     * @param id       id
     * @param isSource 是否使用原文件名
     * @param request  请求
     * @param response 响应
     */
    @GetMapping(value = "/download/{id}")
    public void viewFilesImage(@PathVariable Long id, @RequestParam(required = false) Boolean isSource, HttpServletRequest request, HttpServletResponse response) {
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            FileInfo fileDetails = fileService.getFileDetails(id);
            if (fileDetails == null){
                throw new ServiceException("未找到该文件");
            }
            String filename = (isSource != null && isSource) ? fileDetails.getFileName() : fileDetails.getFilePath();
            inputStream = fileService.getFileInputStream(id);
            response.setHeader("Content-Disposition", "attachment;filename=" + EncodingUtils.convertToFileName(request, filename));
            // 获取输出流
            outputStream = response.getOutputStream();
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            log.error("文件下载出错", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}