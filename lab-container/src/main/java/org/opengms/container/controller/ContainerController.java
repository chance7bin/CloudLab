package org.opengms.container.controller;

import org.opengms.container.controller.common.BaseController;
import org.opengms.container.entity.dto.ApiResponse;
import org.opengms.container.entity.dto.TableDataInfo;
import org.opengms.container.entity.dto.docker.ContainerInfoDTO;
import org.opengms.container.entity.dto.docker.EnvDTO;
import org.opengms.container.enums.ContainerType;
import org.opengms.container.service.IContainerService;
import org.opengms.container.service.IDockerService;
import org.opengms.container.service.IJupyterService;
import org.opengms.container.service.IMSCAsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * docker容器控制层
 *
 * @author bin
 * @date 2022/09/20
 */
@RestController
@RequestMapping("/container")
public class ContainerController extends BaseController {

    @Autowired
    IDockerService dockerService;

    @Autowired
    IJupyterService jupyterService;

    @Autowired
    IContainerService containerService;

    @Autowired
    IMSCAsyncService asyncService;

    // @Anonymous
    @GetMapping(value = "/list")
    public TableDataInfo listContainers() {
        startPage();
        List<ContainerInfoDTO> list = containerService.listContainers(ContainerType.JUPYTER);
        return getDataTable(list);
    }

    @PostMapping(value = "/newEnv")
    public ApiResponse createNewEnv(@RequestBody EnvDTO envDTO){

        Long imageId = containerService.createNewEnv(envDTO);
        asyncService.createNewEnv(envDTO.getContainerId(), envDTO.getEnvName(), envDTO.getTag(), imageId);

        return ApiResponse.success();
    }



}
