package org.opengms.container.service.impl;

import org.opengms.common.utils.file.FileUtils;
import org.opengms.container.constant.ContainerStatus;
import org.opengms.container.entity.dto.docker.JupyterInfoDTO;
import org.opengms.container.entity.po.JupyterContainer;
import org.opengms.container.exception.ServiceException;
import org.opengms.container.mapper.JupyterMapper;
import org.opengms.container.service.IDockerService;
import org.opengms.container.service.IJupyterService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    JupyterMapper jupyterMapper;

    @Autowired
    IDockerService dockerService;


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

    @Override
    public JupyterInfoDTO getJupyterContainerById(Long id) {

        JupyterContainer jc = jupyterMapper.selectById(id);
        if (jc == null){
            throw new ServiceException("未找到该容器");
        }
        if (!ContainerStatus.DELETED.equals(jc.getStatus())){
            String status = dockerService.getContainerStatusByContainerInsId(jc.getContainerInsId());
            jc.setStatus(status);
            jupyterMapper.updateContainerStatus(jc.getContainerId(), status);
        }
        JupyterInfoDTO jupyterInfoDTO = new JupyterInfoDTO();
        BeanUtils.copyProperties(jc, jupyterInfoDTO);
        jupyterInfoDTO.setCreated(jc.getCreateTime());
        return jupyterInfoDTO;

    }

}
