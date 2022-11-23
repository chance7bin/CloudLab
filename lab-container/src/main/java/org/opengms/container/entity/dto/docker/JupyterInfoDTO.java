package org.opengms.container.entity.dto.docker;

import lombok.Data;

/**
 * jupyter容器信息dto
 *
 * @author bin
 * @date 2022/10/13
 */
@Data
public class JupyterInfoDTO extends ContainerInfoDTO{
    /** jupyter的认证token */
    private String jupyterToken;
}
