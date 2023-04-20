package org.opengms.admin.entity.dto.container;

import lombok.Data;
import org.opengms.admin.entity.BaseEntity;
import org.opengms.admin.entity.dto.container.mdl.ModelClass;

/**
 * @author 7bin
 * @date 2022/11/12
 */
@Data
public class ModelService extends BaseEntity {

    /** 模型服务id */
    Long msId;

    /** 关联容器id ( 模型服务是以id为{containerId}的容器为基础环境发布的 ) */
    Long containerId;

    /** 镜像名 */
    // String imageName;

    /** 镜像id 模型基于该镜像启动运行环境 */
    Long imageId;

    /** 模型服务名称 */
    String msName;

    /** 原container中model存放路径  */
    String relativeDir;

    /** mdl文件路径 */
    String mdlFilePath;

    /** 封装脚本路径 */
    String encapScriptPath;

    /** mdl解析实体类 */
    ModelClass modelClass;

    /** 是否部署完成 部署状态 INIT STARTED FINISHED ERROR */
    String deployStatus;

    /** 是否基于当前环境创建新环境 */
    // Boolean newEnv;

    /** 新部署包的id */
    // String pkgId;

    /** 部署包路径 */
    // String deployPkgPath;

    /** 导出的镜像包名 */
    // String imageTar;

    /** 服务类型 (0: 运行完立即销毁; 1: 后台运行) */
    String serviceType;

}
