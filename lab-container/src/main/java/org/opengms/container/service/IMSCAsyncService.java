package org.opengms.container.service;

import org.opengms.container.entity.dto.docker.EnvDTO;
import org.opengms.container.entity.po.ModelService;

/**
 * 异步接口
 *
 * @author 7bin
 * @date 2022/11/07
 */
public interface IMSCAsyncService {

    void exec(String[] cmdArr);

    void exec(String namespace, String podName, String containerName, String[] command);

    /**
     * 创建新环境
     * @param containerId 容器id
     * @param envName 新环境名称
     * @param tag 标签
     * @param imageId 新创建的镜像id，用于镜像的异步更新
     * @author 7bin
     **/
    void createNewEnv(Long containerId, String envName, String tag , Long imageId);
}
