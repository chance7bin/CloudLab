package org.opengms.container.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.opengms.container.entity.po.ModelService;

import java.util.List;

/**
 * @author 7bin
 * @date 2022/11/12
 */
@Mapper
public interface ModelServiceMapper extends BaseMapper<ModelService>{

    int updateDeployStatus(Long msId, String status);

}
