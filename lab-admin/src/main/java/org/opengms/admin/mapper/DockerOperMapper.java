package org.opengms.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.opengms.admin.entity.po.docker.JupyterContainer;
import org.opengms.admin.entity.po.system.SysUser;

import javax.validation.constraints.Max;

@Mapper
public interface DockerOperMapper {

    /**
     * 新增jupyter容器
     *
     * @param jupyterContainer 容器信息
     * @return 结果
     */
    int insertJupyterContainer(JupyterContainer jupyterContainer);

}
