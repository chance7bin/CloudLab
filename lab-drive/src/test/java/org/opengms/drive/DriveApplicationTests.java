package org.opengms.drive;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.opengms.common.utils.file.FileTypeUtils;
import org.opengms.common.utils.file.FileUtils;
import org.opengms.drive.entity.dto.FileDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 7bin
 * @date 2022/11/17
 */
@Slf4j
@SpringBootTest
public class DriveApplicationTests {

    @Value(value = "${drive.repository.workspace}")
    private String workspace;

    //测试获取文件列表
    @Test
    void testGetFilesContainChild(){
        String workspaceDir = workspace + "/workspace";
        List<File> files = FileUtils.ls(workspaceDir);
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
        System.out.println(fileDTOList);
    }

}
