package org.opengms.admin.controller;

import org.opengms.admin.controller.common.BaseController;
import org.opengms.common.utils.http.HttpUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author bin
 * @date 2022/09/20
 */
@RestController
@RequestMapping("/docker")
public class DockerController extends BaseController {

    @GetMapping(value = "/jupyterPage")
    public String jupyterPage() {
        String url = "http://127.0.0.1:8818/lab?token=6543211";
        return HttpUtils.sendGet(url);
    }

}
