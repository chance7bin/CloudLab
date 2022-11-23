package org.opengms.drive.mapper;

import cn.novelweb.tool.http.Result;
import cn.novelweb.tool.upload.local.pojo.UploadFileParam;
import org.apache.ibatis.annotations.Mapper;
import org.opengms.drive.entity.po.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.List;

/**
 * 文件mapper
 *
 * @author 7bin
 * @date 2022/11/22
 */
@Mapper
public interface FileMapper {

    List<FileInfo> selectFileList();

    FileInfo selectById(Long id);

    int insert(FileInfo fileInfo);

    boolean fileIsExist(String fileName);
}
