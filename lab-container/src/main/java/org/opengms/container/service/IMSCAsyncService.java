package org.opengms.container.service;

import org.opengms.container.entity.po.ModelService;

/**
 * @author 7bin
 * @date 2022/11/07
 */
public interface IMSCAsyncService {

    void exec(String[] cmdArr);

    void pkgDispatcher(ModelService modelService);

    void createNewEnv(ModelService modelService);
}
