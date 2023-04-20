package org.opengms.admin.entity.dto.container;

import lombok.Data;
import org.opengms.admin.entity.dto.container.mdl.ModelClass;

/**
 * 模型调用传输对象
 *
 * @author 7bin
 * @date 2023/04/19
 */
@Data
public class InvokeDTO {

    /** 模型服务id */
    Long msId;

    /** 镜像id */
    Long imageId;

    /** 模型服务名称 */
    String msName;

    /** mdl解析实体类 */
    ModelClass modelClass;

    /** 是否部署完成 部署状态 INIT STARTED FINISHED ERROR */
    String deployStatus;

    /** 服务类型 (0: 一次性的; 1: 后台运行)*/
    String serviceType;

}
