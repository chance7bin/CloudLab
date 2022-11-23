package org.opengms.container.service.impl;

import org.opengms.common.utils.file.FileUtils;
import org.opengms.container.service.IJupyterService;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * jupyter服务层
 *
 * @author bin
 * @date 2022/10/6
 */
@Service
public class JupyterServiceImpl implements IJupyterService {



    @Override
    public String generateJupyterConfig(String configDir) {
        return generateJupyterConfig(configDir, null);
    }

    @Override
    public String generateJupyterConfig(String configDir, String suffix) {
        return generateJupyterConfig(configDir, suffix, null);
    }

    @Override
    public String generateJupyterConfig(String configDir, String suffix, String token) {
        try {
            String resourcePath = "static/jupyter_lab_config.py";

            String s = FileUtils.readResourceTxtFile(resourcePath);

            String jupyterToken = token == null ? "66666" : token;

            s += "c.ServerApp.token = '" + jupyterToken + "'";

            String filename = "jupyter_lab_config.py";
            if (suffix != null){
                filename = "jupyter_lab_config" + "_" + suffix + ".py";
            }

            return FileUtils.writeBytes(s.getBytes(), configDir,filename);

            // return true;

        } catch (IOException e) {
            return null;
            // throw new ServiceException("生成jupyter配置文件出错");
        }
    }
}
