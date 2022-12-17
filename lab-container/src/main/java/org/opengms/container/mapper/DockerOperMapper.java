package org.opengms.container.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.opengms.container.entity.po.MsrIns;
import org.opengms.container.entity.po.docker.ContainerInfo;
import org.opengms.container.entity.po.docker.ImageInfo;
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

    int insertContainer(ContainerInfo containerInfo);

    /**
     * 新增镜像
     * @param imageInfo 镜像实体类
     * @return {@link int}
     * @author 7bin
     **/
    int insertImage(ImageInfo imageInfo);

    int countImageByRepository(String imageName);

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

    ContainerInfo getContainerInfoById(Long containerId);

    int updateContainer(ContainerInfo containerInfo);

    JupyterContainer getJupyterContainerInfoById(Long containerId);

    JupyterContainer getJupyterContainerInfoByInsId(String containerInsId);
}
