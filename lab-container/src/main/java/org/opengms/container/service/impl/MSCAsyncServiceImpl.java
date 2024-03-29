package org.opengms.container.service.impl;

import cn.hutool.core.io.FileUtil;
import com.github.dockerjava.api.command.InspectImageResponse;
import io.kubernetes.client.Exec;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1Pod;
import lombok.extern.slf4j.Slf4j;
import org.opengms.common.TerminalRes;
import org.opengms.common.utils.DateUtils;
import org.opengms.common.utils.uuid.UUID;
import org.opengms.container.constant.*;
import org.opengms.container.entity.bo.ExecResponse;
import org.opengms.container.entity.bo.Log;
import org.opengms.container.entity.po.MsrIns;
import org.opengms.container.entity.po.docker.ContainerInfo;
import org.opengms.container.entity.po.docker.ImageInfo;
import org.opengms.container.enums.ContainerType;
import org.opengms.container.enums.ProcessState;
import org.opengms.container.enums.DataFlag;
import org.opengms.container.exception.ServiceException;
import org.opengms.container.mapper.ImageMapper;
import org.opengms.container.mapper.ModelServiceMapper;
import org.opengms.container.mapper.MsrInsMapper;
import org.opengms.container.service.*;
import org.opengms.common.utils.TerminalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author 7bin
 * @date 2022/11/07
 */
@Slf4j
@Service
public class MSCAsyncServiceImpl implements IMSCAsyncService {

    @Autowired
    IMSInsSocketService msInsSocketService;

    @Autowired
    MsrInsMapper msrInsMapper;

    @Value(value = "${container.repository}")
    private String repository;

    @Value(value = "${docker.useDockerHub}")
    private boolean useDockerHub;

    @Autowired
    IDockerService dockerService;

    @Autowired
    IK8sService k8sService;

    @Autowired
    ModelServiceMapper modelServiceMapper;

    // @Autowired
    // IWorkspaceService workspaceService;

    @Autowired
    ImageMapper imageMapper;

    @Autowired
    IContainerService containerService;


    /**
     * 调用终端执行封装脚本 （k8s调用）
     *
     * @param namespace 命名空间
     * @param podName   pod名称
     * @param containerName 容器名称
     * @param command   执行命令
     **/
    @Override
    @Async
    public void exec(String namespace, String podName, String containerName, String[] command){

        MsrIns ins = getMsrInsByPythonCmd(command);

        try {
            ExecResponse res = k8sService.exec(namespace, podName, containerName, command);
            execPostProcessing(res, ins);

        } catch (ApiException | InterruptedException | IOException e) {

            String msg = "[执行终端命令出错] " + e.getMessage();
            log.error(msg);
            execError(ins, msg);

        }


    }


    /**
     * 调用终端执行封装脚本 （docker本地调用）
     *
     * @param cmdArr 执行命令
     * @return void
     * @author 7bin
     **/
    @Override
    @Async
    public void exec(String[] cmdArr) {

        String encapsulationCMD = cmdArr[cmdArr.length - 1];
        String[] encap = encapsulationCMD.split(" ");
        // String msrid = encap[encap.length - 1];
        // MsrIns ins = msInsSocketService.getMsrInsFromMsrInsColl(msrid);
        MsrIns ins = getMsrInsByPythonCmd(encap);

        long start = System.currentTimeMillis();
        // String exe = "python";
        // 在window下用\表示路径，而在linux都是用/表示路径。在有路径需要修改的时候，要注意区分
        // String command = "E:\\opengms-lab\\container\\workspace\\jupyter_cus_5.0_8268889755334766592\\encapsulation.py";
        // String[] cmdArr = new String[] {exe, command, p1, p2, p3, p4, p5, p6, p7, p8, p9};
        log.info("Exec cmd: {}", Arrays.toString(cmdArr));
        // log.info("Exec cmd: {}", command);
        MsrIns currentMsrIns = null;
        try {

            ExecResponse res = dockerService.exec(cmdArr);
            execPostProcessing(res, ins);

        } catch (IOException | InterruptedException e) {
            String msg = "[执行终端命令出错] " + e.getMessage();
            log.error(msg);
            execError(ins, msg);
            // if (currentMsrIns != null){
            // currentMsrIns.getLogs().add(new Log("执行终端命令出错: " + e.getMessage()));
            // }
        }

    }

    // exec后处理
    private void execPostProcessing(ExecResponse res, MsrIns ins){

        // exitVal == 0 为程序执行成功
        // exitVal == 1 为程序异常终止
        // exitVal == -1 为程序执行成功, 但自定义返回错误代码
        int exitVal = res.getExitCode();
        String response = res.getResponse();
        String error = res.getError();

        if (exitVal == 0) {
            // log.info("Exec done, cost: " + ((end - start) / 1000) + "s");
            log.info("[程序正常退出] 退出码 : {} (response: {})", exitVal, response);
        } else if (exitVal == -1) {
            String msg = "[程序自定义错误] 退出码 : " + exitVal + "(response: " + error + ")";
            log.error(msg);
            execError(ins, msg);
        } else {
            String msg = "[程序内部错误] 退出码 : " + exitVal + "(error: " + error + ")";
            log.error(msg);
            execError(ins, msg);
        }
    }

    /**
     * 根据python的调用命令获取模型服务实例id
     * @param command python调用命令
     * @return {@link MsrIns}
     * @author 7bin
     **/
    private MsrIns getMsrInsByPythonCmd(String[] command) {

        String msrid = command[command.length - 1];
        MsrIns ins = msInsSocketService.getMsrInsFromMsrInsColl(msrid);
        return ins;

    }

    // @Async
    public void pkgDispatcher(String envName, String imgTarName, String tag, Long imageId) {

        // 把压缩包解压到 /service/{containerId} 文件夹下
        // File zipFile = new File(repository + ContainerConstants.PACKAGE_PATH(modelService.getPkgId()));
        // String outFilePathStr = repository + ContainerConstants.SERVICE_DIR(modelService.getContainerId()) + "/" + modelService.getMsId();
        // File outFileDir = new File(outFilePathStr);
        // ZipUtil.unzip(zipFile, outFileDir);

        // 将镜像导入到docker中
        String inputPath = repository + ContainerConstants.IMAGE_PATH + "/" + imgTarName;
        // int cnt = imageMapper.countImageByRepository(envName);
        // cnt++;
        // while (imageMapper.isExistByRepoTags(envName + ":" + cnt + ".0") > 0){
        //     cnt++;
        // }
        String imageName = envName + ":" + tag;
        TerminalRes res = dockerService.importContainer(inputPath, imageName);
        if (TerminalRes.SUCCESS_CODE.equals(res.getCode())){

            // 更新image表
            ImageInfo imageInfo = imageMapper.selectById(imageId);
            imageInfo.setImageId(res.getMsg());
            // imageInfo.setTag(tag);
            // imageInfo.setRepoTags(imageName);
            imageInfo.setSize(FileUtil.size(new File(inputPath)));
            imageInfo.setStatus(ImageStatus.FINISHED);
            imageMapper.updateById(imageInfo);
            log.info("导入镜像成功，镜像名：" + imageName);

            // 删除镜像包
            // FileUtil.del(inputPath);

            // 更新modelService
            // modelService.setDeployStatus(DeployStatus.FINISHED);
            // modelService.setImageId(imageInfo.getId());
            // (Deprecated)重置模型服务关联的容器id
            // modelService.setContainerId(null);
            // modelServiceMapper.updateById(modelService);
        } else {
            // 更新image表
            ImageInfo imageInfo = imageMapper.selectById(imageId);
            imageInfo.setStatus(ImageStatus.ERROR);
            imageMapper.updateById(imageInfo);
            log.info("导入镜像出错，镜像名：" + imageName);
            throw new ServiceException("导入镜像出错");
        }

    }

    @Async
    @Override
    public void createNewEnv(Long containerId, String envName, String tag, Long imageId) {

        // 根据当前环境生成镜像包
        ContainerInfo container = containerService.getContainerInfoById(containerId, ContainerType.JUPYTER);
        if (container == null){
            imageMapper.updateStatusById(imageId, ImageStatus.ERROR);
            throw new ServiceException("未找到对应的容器");
        }

        // 容器未启动要先启动容器(export要，commit不用)
        // if (!dockerService.isContainerRunning(container.getContainerInsId())){
        //     dockerService.startContainer(container.getContainerInsId());
        //     containerService.updateContainerStatus(container.getContainerId(), ContainerStatus.RUNNING, ContainerType.JUPYTER);
        // }

        // 导出镜像的uuid
        String pkgUUID = UUID.fastUUID().toString();
        String imgTarName = pkgUUID + ".tar";

        // TerminalRes terminalRes = dockerService.exportContainer(container.getContainerInsId(),
        //     repository + ContainerConstants.IMAGE_PATH + "/" + imgTarName);
        // if (terminalRes.getCode().equals(TerminalRes.ERROR_CODE)){
        //     throw new ServiceException("导出镜像出错");
        // }
        try {
            // 使用commit， 不使用export
            String sha256 = dockerService.commitContainer(container.getContainerInsId(), envName, tag);
            // 更新image表
            ImageInfo imageInfo = imageMapper.selectById(imageId);
            imageInfo.setImageId(sha256);
            ImageInfo inspectImageResponse = dockerService.inspectImage(sha256);
            imageInfo.setSize(inspectImageResponse.getSize());
            // imageInfo.setCommitCount(xxx);
            imageInfo.setStatus(ImageStatus.COMMITTED);
            imageMapper.updateById(imageInfo);
            log.info("镜像commit成功，镜像名：" + imageInfo.getRepoTags());

        }catch (Exception e){
            // 更新image表
            // ImageInfo imageInfo = imageMapper.selectById(imageId);
            // imageInfo.setStatus(ImageStatus.ERROR);
            imageMapper.updateStatusById(imageId, ImageStatus.ERROR);
            log.info("镜像commit出错，镜像id：" + imageId);
            throw new ServiceException("镜像commit出错");
        }

        if (useDockerHub){
            try {
                dockerService.pushImage(envName, tag);
                log.info("镜像push成功");
                imageMapper.updateStatusById(imageId, ImageStatus.PUSHED);
            } catch (InterruptedException e) {
                // 更新image表
                imageMapper.updateStatusById(imageId, ImageStatus.ERROR);
                log.info("镜像push出错，镜像id：" + imageId);
                throw new ServiceException("镜像push出错");

            }
        }

        // 压缩打包
        // String zipFile = ContainerConstants.PACKAGE_PATH(pkgUUID);
        // ZipUtil.zip(
        //     new File(repository + zipFile),
        //     true,
        //     new File(repository + "/workspace/" + modelService.getContainerId() + "/config"),
        //     new File(repository + ContainerConstants.SERVICE_DIR(modelService.getContainerId()) + "/" + modelService.getRelativeDir() + "/model"),
        //     new File(repository + ContainerConstants.IMAGE_PATH + "/" + imgTarName));
        // ZipUtil.zip(
        //     new File(repository + zipFile),
        //     true,
        //     new File(repository + "/workspace/" + modelService.getContainerId() + "/config"),
        //     new File(repository + ContainerConstants.SERVICE_DIR(modelService.getContainerId()) + "/" + modelService.getMsId() + "/model"),
        //     new File(repository + ContainerConstants.IMAGE_PATH + "/" + imgTarName));

        // 打包后删除该镜像tar包
        // FileUtil.del(new File(repository + ContainerConstants.IMAGE_PATH + "/" + pkgUUID + ".tar"));

        // modelService.setPkgId(pkgUUID);
        // modelService.setImageTar(imgTarName);
        // modelServiceMapper.updateById(modelService);

        // 部署包分发器
        // pkgDispatcher(envName, imgTarName, tag, imageId);


    }

    /**
     * 脚本执行出错的话调用统一方法给模型实例添加日志
     * @param ins 模型实例
     * @param msg 错误信息
     * @author 7bin
     **/
    private void execError(MsrIns ins, String msg){
        ins.setStatus(TaskStatus.ERROR);

        ins.getLogs().add(new Log(
            ProcessState.RUN_SCRIPT,
            null, null,
            DataFlag.ERROR,
            msg,
            new Date()
        ));

        if (ins.getStartTime() == null){
            ins.setSpanTime(0);
        } else {
            int second = DateUtils.differentSecondsByMillisecond(new Date(), ins.getStartTime());
            ins.setSpanTime(second);
        }
        msrInsMapper.updateById(ins);

        msInsSocketService.removeChannelAndMsrInsColl(ins);

    }
}
