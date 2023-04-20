package org.opengms.admin.controller.container;

import org.opengms.admin.clients.container.MsInsClient;
import org.opengms.admin.controller.common.BaseController;
import org.opengms.admin.entity.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模型运行实例控制层
 *
 * @author 7bin
 * @date 2022/12/05
 */
@RestController
@RequestMapping("/container/instance")
public class MsInsController extends BaseController {

    @Autowired
    MsInsClient msInsClient;

    @GetMapping(value = "/{msriId}")
    public ApiResponse getMsInsById(@PathVariable("msriId") String msriId){
        ApiResponse ins = msInsClient.getMsInsById(msriId);
        return ins;
    }

}
