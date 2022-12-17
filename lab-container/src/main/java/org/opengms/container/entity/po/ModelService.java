package org.opengms.container.entity.po;

import lombok.Data;
import org.opengms.container.entity.BaseEntity;
import org.opengms.container.entity.bo.mdl.ModelClass;

/**
 * @author 7bin
 * @date 2022/11/12
 */
@Data
public class ModelService extends BaseEntity {

    /** 模型服务id */
    Long msId;

    /** 关联容器id 该属性为空说明该服务的运行容器是动态生成的 */
    Long containerId;

    /** 镜像名 */
    String imageName;

    /** 模型服务名称 */
    String msName;

    /** 相对于 /service 的路径 模型运行相关文件夹路径 该路径下包括 assembly/instance/model 等文件夹  */
    String relativeDir;

    /** mdl文件路径 */
    String mdlFilePath;

    /** 封装脚本路径 */
    String encapScriptPath;

    /** mdl解析实体类 */
    ModelClass modelClass;

    /** 是否部署完成 */
    Boolean deployStatus;

    /** 是否基于当前环境创建新镜像 */
    Boolean newImage;

    /** 部署包路径 */
    String deployPkgPath;

    /** 镜像名 */
    String imageTar;

    /** 新部署包的id */
    String newPkgId;


}
