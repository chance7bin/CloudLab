package org.opengms.container.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.opengms.container.entity.po.ModelService;

import java.util.List;

/**
 * @author 7bin
 * @date 2022/11/12
 */
@Mapper
public interface ModelServiceMapper {

    int insertModelService(ModelService modelService);

    List<ModelService> selectServiceList();

    ModelService getModelServiceById(String msId);

    int updateModelService(ModelService modelService);
}
