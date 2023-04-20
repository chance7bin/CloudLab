package org.opengms.container.entity.bo;

import lombok.Data;
import org.apache.poi.ss.formula.functions.T;
import org.opengms.container.entity.po.docker.ContainerInfo;
import org.opengms.container.mapper.BaseContainerMapper;

/**
 * 容器相关的一些bean
 *
 * @author 7bin
 * @date 2023/04/10
 */
@Data
public class ContainerBean {

    BaseContainerMapper<? extends ContainerInfo> mapper;

    ContainerInfo containerInfo;

}
