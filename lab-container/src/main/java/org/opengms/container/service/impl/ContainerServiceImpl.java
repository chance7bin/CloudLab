package org.opengms.container.service.impl;

import cn.hutool.core.io.FileUtil;
import com.github.dockerjava.api.model.Container;
import lombok.extern.slf4j.Slf4j;
import org.opengms.common.utils.DateUtils;
import org.opengms.common.utils.uuid.SnowFlake;
import org.opengms.container.entity.bo.ContainerBean;
import org.opengms.container.entity.dto.docker.ContainerInfoDTO;
import org.opengms.container.entity.dto.docker.EnvDTO;
import org.opengms.container.entity.po.ContainerRelation;
import org.opengms.container.entity.po.JupyterContainer;
import org.opengms.container.entity.po.docker.ContainerInfo;
import org.opengms.container.entity.po.docker.ImageInfo;
import org.opengms.container.enums.ContainerType;
import org.opengms.container.exception.ServiceException;
import org.opengms.container.mapper.BaseContainerMapper;
import org.opengms.container.mapper.ContainerRelationMapper;
import org.opengms.container.mapper.ImageMapper;
import org.opengms.container.mapper.JupyterMapper;
import org.opengms.container.service.IContainerService;
import org.opengms.container.service.IDockerService;
import org.opengms.container.service.IMSCAsyncService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 7bin
 * @date 2023/04/10
 */
@Slf4j
@Service
public class ContainerServiceImpl implements IContainerService {

    @Autowired
    JupyterMapper jupyterMapper;

    @Autowired
    IDockerService dockerService;

    @Autowired
    ImageMapper imageMapper;

    // @Autowired
    // IMSCAsyncService asyncService;

    @Autowired
    ContainerRelationMapper containerRelationMapper;

    private ContainerBean beanFactory(ContainerType type){

        ContainerBean bean = new ContainerBean();

        switch (type){
            case JUPYTER:{
                bean.setMapper(jupyterMapper);
                break;
            }
            case WEBSITE:{
                bean.setMapper(jupyterMapper);
                break;
            }
            default:{
                bean.setMapper(jupyterMapper);
                break;
            }
        }

        return bean;

    }

    private BaseContainerMapper getMapper(ContainerType type){
        ContainerBean factory = beanFactory(type);
        return factory.getMapper();
    }

    @Override
    public ContainerInfo getContainerInfoById(Long id, ContainerType type) {
        ContainerInfo info = (ContainerInfo) getMapper(type).selectById(id);
        return info;
    }

    @Override
    public ContainerInfo getContainerInfoByInsId(String insId, ContainerType type) {
        ContainerInfo info = (ContainerInfo) getMapper(type).getContainerInfoByInsId(insId);
        return info;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteContainer(Long containerId, ContainerType type) {
        int cnt = getMapper(type).deleteById(containerId);
        int cnt2 = containerRelationMapper.deleteRelationByContainerId(containerId);
        return cnt == 1 && cnt2 == 1 ? 1 : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertContainer(ContainerInfo containerInfo, ContainerType type) {

        String containerInsId = containerInfo.getContainerInsId();

        // 写在操作数据库后面不会执行
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                // 实现业务逻辑，在前面分析的invokeAfterCommit方法中，会调用到这里
                if (status == TransactionSynchronization.STATUS_COMMITTED) {
                    // 事务提交后执行
                    log.info("容器信息插入成功");
                } else if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    // 事务回滚后执行
                    log.warn("容器信息插入失败，销毁容器");
                    dockerService.removeContainer(containerInsId);
                }
            }
        });

        // 插入的时候一定要是继承ContainerInfo的对象，不然会报错：There is no getter for property named 'xxx'

        int cnt = getMapper(type).insert(containerInfo);
        // 添加container_relation
        int cnt2 = bindRelationWithContainerAndType(containerInfo, ContainerType.JUPYTER);

        return cnt == 1 && cnt2 == 1 ? 1 : 0;

    }

    @Override
    public List<ContainerInfo> listContainers(ContainerType type) {

        BaseContainerMapper mapper = getMapper(type);
        List<ContainerInfo> list = mapper.selectList();
        return list;
    }



    @Override
    public List<Integer> listAllUsedPortByHostIP(String hostIP) {
        return null;
    }


    @Override
    public List<Integer> listAllUsedPortByHostMAC(String hostMAC) {
        return null;
    }

    @Override
    public int bindRelationWithContainerAndType(ContainerInfo container, ContainerType type) {

        ContainerRelation containerRelation = new ContainerRelation();
        containerRelation.setContainerId(container.getContainerId());
        containerRelation.setTypeCode(type.getCode());
        if (container.getHostIP() != null){
            containerRelation.setHostIP(container.getHostIP());
        }
        if (container.getHostIP() != null){
            containerRelation.setHostMAC(container.getHostMAC());
        }
        if (container.getHostIP() != null){
            containerRelation.setPort(container.getHostBindPort());
        }
        return containerRelationMapper.insert(containerRelation);
    }

    @Override
    public int updateContainerStatus(Long containerId, String status, ContainerType type) {

        BaseContainerMapper mapper = getMapper(type);
        return mapper.updateContainerStatus(containerId,status);
    }

    @Override
    public Long createNewEnv(EnvDTO envDTO) {

        // 判断是否存在名称和tag都相同的镜像
        String repoTags = envDTO.getEnvName() + ":" + envDTO.getTag();
        ImageInfo info = imageMapper.getImageInfoByRepoTags(repoTags);
        if (info != null){
            throw new ServiceException("[ " + repoTags + " ] 环境已存在");
        }

        ImageInfo imageInfo = new ImageInfo();
        imageInfo.setId(SnowFlake.nextId());
        imageInfo.setImageName(envDTO.getEnvName());
        imageInfo.setTag(envDTO.getTag());
        imageInfo.setRepoTags(repoTags);
        Integer cnt = imageMapper.insert(imageInfo);
        if (cnt <= 0){
            throw new ServiceException("创建环境失败");
        }
        return imageInfo.getId();

    }
}
