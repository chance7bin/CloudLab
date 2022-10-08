package org.opengms.admin.service;


import org.opengms.common.utils.file.FileUtils;

import java.io.IOException;

public interface IJupyterService {

    Boolean generateJupyterConfig(String configDir);

}
