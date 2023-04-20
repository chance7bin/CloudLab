package org.opengms.container.entity.vo;

import lombok.Data;
import org.opengms.container.entity.bo.mdl.ModelClass;

/**
 * @author 7bin
 * @date 2023/04/17
 */
@Data
public class ModelServiceVO {

    /** 模型服务id */
    Long msId;

    /** 关联容器id 该属性为空说明该服务的运行容器是根据 imageTar 动态生成的 */
    // Long containerId;

    /** 镜像名 */
    // String imageName;

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
