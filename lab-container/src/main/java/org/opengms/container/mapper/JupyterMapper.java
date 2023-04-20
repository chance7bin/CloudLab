package org.opengms.container.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.opengms.container.entity.po.JupyterContainer;

/**
 * jupyter 持久层
 *
 * @author 7bin
 * @date 2023/04/10
 */
@Mapper
public interface JupyterMapper extends BaseContainerMapper<JupyterContainer> {


}
