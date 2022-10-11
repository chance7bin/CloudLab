package org.opengms.admin.service;


import org.opengms.common.utils.file.FileUtils;

import java.io.IOException;

/**
 * jupyter接口
 *
 * @author bin
 * @date 2022/10/6
 */
public interface IJupyterService {

    /**
     * 根据config的文件夹路径生成jupyter配置文件
     * @param configDir
     * @return 生成成功返回该配置文件路径，失败返回null
     * @Author bin
     **/
    String generateJupyterConfig(String configDir);

}
