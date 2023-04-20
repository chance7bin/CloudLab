package org.opengms.container.service.impl;

import org.opengms.container.entity.bo.ContainerBean;
import org.opengms.container.entity.po.docker.ContainerInfo;
import org.opengms.container.enums.ContainerType;
import org.opengms.container.mapper.BaseContainerMapper;
import org.opengms.container.mapper.JupyterMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 异步方法中调用的其他service的方法 防止循环依赖
 *
 * @author 7bin
 * @date 2023/04/18
 */
@Service
public class AsyncCommonService {


}
