package org.opengms.container.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengms.container.entity.po.JupyterContainer;

/**
 * docker 容器类型
 *
 * @author 7bin
 * @date 2023/04/10
 */
@AllArgsConstructor
public enum ContainerType {

    JUPYTER(0, JupyterContainer.class),
    WEBSITE(1, JupyterContainer.class),
    ;

    private final Integer code;
    private final Class clazz;


    public Integer getCode() {
        return code;
    }

    public Class getClazz() {
        return clazz;
    }
}
