package org.opengms.admin.entity.dto.container;

import lombok.Data;
import org.opengms.admin.entity.BaseEntity;

/**
 * @author 7bin
 * @date 2022/11/12
 */
@Data
public class ModelServiceDTO extends BaseEntity {

    /** 关联容器id */
    Long containerId;

    /** 关联容器名称 */
    String containerName;

    /** 模型服务名称 */
    String msName;

    /** 模型运行相关文件夹路径 */
    String relativeDir;

    /** mdl文件路径 */
    String mdlFilePath;

    /** 封装脚本路径 */
    String encapScriptPath;

}
