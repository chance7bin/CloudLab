package org.opengms.admin.service.impl;

import org.opengms.admin.exception.ServiceException;
import org.opengms.admin.service.IJupyterService;
import org.opengms.common.utils.file.FileUtils;
import org.springframework.beans.factory.annotation.Value;
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

        try {
            String resourcePath = "static/jupyter_lab_config.py";

            String s = FileUtils.readResourceTxtFile(resourcePath);

            // System.out.println(s);

            String jupyterToken = "66666";

            s += "c.ServerApp.token = '" + jupyterToken + "'";

            return FileUtils.writeBytes(s.getBytes(), configDir,"jupyter_lab_config.py");

            // return true;

        } catch (IOException e) {
            return null;
            // throw new ServiceException("生成jupyter配置文件出错");
        }

    }
}
