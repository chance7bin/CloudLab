package org.opengms.admin.service.impl;

import org.opengms.admin.exception.ServiceException;
import org.opengms.admin.service.IJupyterService;
import org.opengms.common.utils.file.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class JupyterServiceImpl implements IJupyterService {



    @Override
    public Boolean generateJupyterConfig(String configDir) {

        try {
            String resourcePath = "static/jupyter_lab_config.py";

            String s = FileUtils.readResourceTxtFile(resourcePath);

            // System.out.println(s);

            String jupyterToken = "66666";

            s += "c.ServerApp.token = '" + jupyterToken + "'";

            FileUtils.writeBytes(s.getBytes(), configDir,"jupyter_lab_config.py");

            return true;

        } catch (IOException e) {
            return false;
            // throw new ServiceException("生成jupyter配置文件出错");
        }

    }
}
