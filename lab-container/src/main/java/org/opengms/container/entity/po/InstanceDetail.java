package org.opengms.container.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.opengms.container.entity.bo.mdl.ModelClass;
import org.opengms.container.entity.po.ModelService;
import org.opengms.container.entity.po.MsrIns;

import java.util.Date;

/**
 * 运行实例的详细传输对象
 *
 * @author 7bin
 * @date 2023/06/05
 */
@Data
public class InstanceDetail {

    String serviceName;

    // ModelClass modelClass;

    String serviceCreteBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    Date serviceCreateTime;

    MsrIns msrIns;

}
