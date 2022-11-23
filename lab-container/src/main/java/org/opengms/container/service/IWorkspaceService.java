package org.opengms.container.service;


/**
 * 工作空间接口
 *
 * @author bin
 * @date 2022/10/6
 */
public interface IWorkspaceService {

    /**
     * 初始化工作空间
     * @param username 用户名
     * @param imageName 镜像名
     * @return 是否初始化成功
     * @author bin
     **/
    boolean initWorkspace(String username, String imageName);
}
