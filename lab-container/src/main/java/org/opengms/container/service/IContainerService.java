package org.opengms.container.service;

import org.apache.poi.ss.formula.functions.T;
import org.opengms.container.entity.dto.docker.ContainerInfoDTO;
import org.opengms.container.entity.dto.docker.EnvDTO;
import org.opengms.container.entity.po.docker.ContainerInfo;
import org.opengms.container.enums.ContainerType;

import java.util.List;

/**
 * 容器操作 通用方法 业务层
 *
 * @author 7bin
 * @date 2023/04/10
 */
public interface IContainerService {

    ContainerInfo getContainerInfoById(Long id, ContainerType type);

    ContainerInfo getContainerInfoByInsId(String insId, ContainerType type);

    int deleteContainer(Long containerId, ContainerType type);

    int insertContainer(ContainerInfo containerInfo, ContainerType type);

    List<ContainerInfoDTO> listContainers(ContainerType type);

    /**
     * 根据主机ip获取已经使用的端口
     * @param hostIP
     * @return {@link List<Integer>}
     * @author 7bin
     **/
    List<Integer> listAllUsedPortByHostIP(String hostIP);

    /**
     * 根据主机MAC获取已经使用的端口
     * @param hostMAC
     * @return {@link List<Integer>}
     * @author 7bin
     **/
    List<Integer> listAllUsedPortByHostMAC(String hostMAC);


    /**
     * 绑定容器与类型之间的关系
     * @param container 容器实例
     * @return {@link int}
     * @author 7bin
     **/
    int bindRelationWithContainerAndType(ContainerInfo container, ContainerType type);


    int updateContainerStatus(Long containerId, String status, ContainerType type);

    /**
     * 创建新环境
     * @param envDTO env数据创数对象
     * @return {@link Long} 新创建的环境id
     * @author 7bin
     **/
    Long createNewEnv(EnvDTO envDTO);
}
