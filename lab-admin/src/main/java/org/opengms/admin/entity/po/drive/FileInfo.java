package org.opengms.admin.entity.po.drive;


import lombok.Data;
import org.opengms.admin.entity.BaseEntity;

/**
 * 文件系统信息
 *
 * @author 7bin
 * @date 2022/11/29
 */
@Data
public class FileInfo extends BaseEntity {

    /** 文件id */
    Long fileId;

    /** 父文件id 根目录无parentId 父目录为根目录的parentId为root */
    String parentId;

    /** 文件名 */
    String filename;

    /** 是否为文件夹 */
    Boolean directory;

    /** 文件md5 如果是文件的话 */
    String md5;

    /** 相对于云盘根目录的路径 */
    String path;

    /** 文件大小 */
    String size;

    /** 文件类型 */
    String type;

    /** 下载的fileId[/drive/file/download/{driveFileId}] 该id为Drive的fileId字段 */
    String driveFileId;


}
