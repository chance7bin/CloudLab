package org.opengms.container.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.opengms.container.entity.po.docker.JupyterContainer;

import java.util.List;


/**
 * Docker 数据层
 *
 * @author bin
 * @date 2022/10/6
 */
@Mapper
public interface DockerOperMapper {

    /**
     * 新增jupyter容器
     *
     * @param jupyterContainer 容器信息
     * @return 结果
     */
    int insertJupyterContainer(JupyterContainer jupyterContainer);

    /**
     * 获取容器列表
     * @param
     * @return java.util.List<org.opengms.admin.entity.po.docker.JupyterContainer>
     * @Author bin
     **/
    List<JupyterContainer> listAll();

    /**
     * 获取已经占用的端口
     * @param
     * @return java.util.List<java.lang.Integer>
     * @Author bin
     **/
    List<Integer> listAllUsedPort();


    JupyterContainer getContainerInfoById(Long containerId);

    JupyterContainer getContainerInfoByInsId(String containerInsId);
}
