package org.opengms.container.service;

import org.opengms.container.entity.dto.ModelServiceDTO;
import org.opengms.container.entity.po.ModelService;

import java.util.List;

/**
 * moxingfuw
 *
 * @author 7bin
 * @date 2022/11/07
 */
public interface IMSService {

    void invoke(ModelService modelService);

    int insertModelService(ModelServiceDTO ModelServiceDTO);


    List<ModelService> selectServiceList();

    ModelService getModelServiceById(String msId);
}
