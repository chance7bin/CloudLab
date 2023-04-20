package org.opengms.container.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.opengms.container.entity.po.ContainerRelation;
import org.opengms.container.enums.ContainerType;

import java.util.List;

/**
 * 容器跟类型的关联，用于分表查询
 *
 * @author 7bin
 * @date 2023/04/11
 */
@Mapper
public interface ContainerRelationMapper extends BaseMapper<ContainerRelation>{



    /**
     * 查询所有主机中已使用的端口
     * @param
     * @return {@link ContainerRelation}
     * @author 7bin
     **/
    List<ContainerRelation> selectHostAndPort();

    /**
     * 根据ip获取已使用的端口列表
     * @param ip 主机ip
     * @return {@link List<Integer>}
     * @author 7bin
     **/
    List<Integer> listUsedPortByIP(String ip);

    /**
     * 根据ip查询该主机下数值最大的已使用端口
     * @param hostIp 主机ip
     * @return {@link ContainerRelation}
     * @author 7bin
     **/
    ContainerRelation findLastUsedPortByHostIP(String hostIp);

    /**
     * 根据容器id删除关联
     * @param containerId 容器id
     * @return {@link int}
     * @author 7bin
     **/
    int deleteRelationByContainerId(Long containerId);


}
