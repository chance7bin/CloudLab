package org.opengms.container.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.ss.formula.functions.T;
import org.opengms.container.entity.po.docker.ContainerInfo;
import org.opengms.container.entity.po.docker.ImageInfo;
import org.opengms.container.entity.po.JupyterContainer;
import org.opengms.container.enums.ContainerType;

import java.util.List;


/**
 * Docker容器 通用方法 接口
 *
 * @author bin
 * @date 2022/10/6
 */
public interface BaseContainerMapper<T extends ContainerInfo> extends BaseMapper<T>{


    T getContainerInfoByInsId(String containerInsId);

    int updateContainerStatus(@Param("containerId") Long containerId, @Param("status") String status);

    int updateContainerInsId(@Param("containerId") Long containerId, @Param("insId") String insId);

}
