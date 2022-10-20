package org.opengms.admin.entity.po.docker;


import lombok.Data;
import org.opengms.common.utils.uuid.SnowFlake;

import java.util.List;

/**
 * jupyter容器信息
 *
 * @author bin
 * @date 2022/10/6
 */
@Data
public class JupyterContainer extends ContainerInfo{

    private static final long serialVersionUID = 1L;

    /** 工作空间数据卷挂载 */
    String workspaceVolume;

    /** 配置文件数据卷挂载 */
    String configVolume;

    /** jupyter的认证token */
    String jupyterToken;

}
