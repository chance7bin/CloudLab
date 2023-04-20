package org.opengms.container.service;

import org.opengms.container.entity.dto.InvokeDTO;
import org.opengms.container.entity.dto.ModelServiceDTO;
import org.opengms.container.entity.po.ModelService;

import java.util.List;

/**
 * 模型服务
 *
 * @author 7bin
 * @date 2022/11/07
 */
public interface IMSService {

    /**
     *
     * @param invokeDTO 模型服务实例
     * @return {@link String} 模型运行实例id
     * @author 7bin
     **/
    String invoke(InvokeDTO invokeDTO);

    int insertModelService(ModelServiceDTO ModelServiceDTO);


    List<ModelService> selectServiceList();

    ModelService getModelServiceById(String msId);


}
