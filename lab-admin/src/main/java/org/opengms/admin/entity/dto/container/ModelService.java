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

    /** 关联容器id */
    Long containerId;

    /** 镜像名 */
    String imageName;

    /** 模型服务名称 */
    String msName;

    /** 模型运行相关文件夹路径 */
    String relativeDir;

    /** mdl文件路径 */
    String mdlFilePath;

    /** 封装脚本路径 */
    String encapScriptPath;

    /** mdl解析实体类 */
    ModelClass modelClass;

    /** 是否部署完成 */
    Boolean deployStatus;

    /** 新部署包的id */
    String newPkgId;

    /** 部署包路径 */
    String deployPkgPath;

    /** 镜像名 */
    String imageTar;

    /** 是否基于当前环境创建新镜像 */
    Boolean newImage;
}
