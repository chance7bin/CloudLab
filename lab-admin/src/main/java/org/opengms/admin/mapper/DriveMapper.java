package org.opengms.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.opengms.admin.entity.po.drive.FileInfo;

import java.util.List;

/**
 * @author 7bin
 * @date 2022/11/29
 */
@Mapper
public interface DriveMapper {

    List<FileInfo> selectFileByUser(@Param("parentId") String parentId, @Param("username") String username);

    int insert(FileInfo fileInfo);

    /**
     * 是否在一个路径中存在相同文件名的文件
     * @param filename
     * @param parentId
     * @return {@link FileInfo}
     * @author 7bin
     **/
    FileInfo existFileInPath(@Param("filename") String filename,@Param("parentId") String parentId);
}
