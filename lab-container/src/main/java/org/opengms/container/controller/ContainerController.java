package org.opengms.container.controller;

import org.opengms.container.controller.common.BaseController;
import org.opengms.container.entity.dto.ApiResponse;
import org.opengms.container.entity.dto.TableDataInfo;
import org.opengms.container.entity.dto.docker.ContainerInfoDTO;
import org.opengms.container.entity.dto.docker.EnvDTO;
import org.opengms.container.entity.po.docker.ContainerInfo;
import org.opengms.container.enums.ContainerType;
import org.opengms.container.service.IContainerService;
import org.opengms.container.service.IDockerService;
import org.opengms.container.service.IJupyterService;
import org.opengms.container.service.IMSCAsyncService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
        List<ContainerInfo> list = containerService.listContainers(ContainerType.JUPYTER);
        TableDataInfo dataTable = getDataTable(list);
        List<ContainerInfoDTO> res = new ArrayList<>();
        for (ContainerInfo info : list) {
            ContainerInfoDTO containerInfoDTO = new ContainerInfoDTO();
            BeanUtils.copyProperties(info, containerInfoDTO);
            containerInfoDTO.setCreated(info.getCreateTime());
            res.add(containerInfoDTO);
        }
        dataTable.setRows(res);
        return dataTable;
    }

    @PostMapping(value = "/newEnv")
    public ApiResponse createNewEnv(@RequestBody EnvDTO envDTO){

        Long imageId = containerService.createNewEnv(envDTO);
        asyncService.createNewEnv(envDTO.getContainerId(), envDTO.getEnvName(), envDTO.getTag(), imageId);

        return ApiResponse.success();
    }



}
