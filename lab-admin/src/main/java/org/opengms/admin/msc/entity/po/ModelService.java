package org.opengms.admin.msc.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengms.admin.entity.SerializableEntity;
import org.opengms.admin.msc.entity.BaseEntity;
import org.opengms.admin.msc.entity.bo.mdl.ModelClass;

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
}
