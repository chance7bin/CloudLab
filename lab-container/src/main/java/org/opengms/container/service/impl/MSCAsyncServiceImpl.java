package org.opengms.container.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.opengms.common.TerminalRes;
import org.opengms.common.utils.DateUtils;
import org.opengms.common.utils.uuid.SnowFlake;
import org.opengms.common.utils.uuid.UUID;
import org.opengms.container.constant.ContainerConstants;
import org.opengms.container.constant.TaskStatus;
import org.opengms.container.entity.bo.Log;
import org.opengms.container.entity.dto.docker.JupyterInfoDTO;
import org.opengms.container.entity.po.ModelService;
import org.opengms.container.entity.po.MsrIns;
import org.opengms.container.entity.po.docker.ImageInfo;
import org.opengms.container.enums.ProcessState;
import org.opengms.container.enums.ProcessStatus;
import org.opengms.container.exception.ServiceException;
import org.opengms.container.mapper.DockerOperMapper;
import org.opengms.container.mapper.ModelServiceMapper;
import org.opengms.container.mapper.MsrInsMapper;
import org.opengms.container.service.IDockerService;
import org.opengms.container.service.IMSCAsyncService;
import org.opengms.container.service.IMSInsService;
import org.opengms.common.utils.TerminalUtils;
import org.opengms.container.service.IWorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.Date;

/**
 * @author 7bin
 * @date 2022/11/07
 */
@Slf4j
@Service
public class MSCAsyncServiceImpl implements IMSCAsyncService {

    @Autowired
    IMSInsService msInsService;

    @Autowired
    MsrInsMapper msrInsMapper;

    @Value(value = "${container.repository}")
    private String repository;

    @Autowired
    IDockerService dockerService;

    @Autowired
    ModelServiceMapper modelServiceMapper;

    @Autowired
    IWorkspaceService workspaceService;

    @Autowired
    DockerOperMapper dockerOperMapper;

    /**
     * 调用终端执行封装脚本
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
        String msrid = encap[encap.length - 1];
        MsrIns ins = msInsService.getMsrInsFromMsrInsColl(msrid);

        long start = System.currentTimeMillis();
        // String exe = "python";
        // 在window下用\表示路径，而在linux都是用/表示路径。在有路径需要修改的时候，要注意区分
        // String command = "E:\\opengms-lab\\container\\workspace\\jupyter_cus_5.0_8268889755334766592\\encapsulation.py";
        // String[] cmdArr = new String[] {exe, command, p1, p2, p3, p4, p5, p6, p7, p8, p9};
        log.info("Exec cmd: {}", Arrays.toString(cmdArr));
        // log.info("Exec cmd: {}", command);
        MsrIns currentMsrIns = null;
        try {
            //这个方法是类似隐形开启了命令执行器，输入指令执行python脚本
            Process process = Runtime.getRuntime()
                .exec(cmdArr); // "python解释器位置（这里一定要用python解释器所在位置不要用python这个指令）+ python脚本所在路径（一定绝对路径）"

            String response = TerminalUtils.getInputMsg(process);
            String error = TerminalUtils.getErrorMsg(process);

            int exitVal = process.waitFor(); // 阻塞程序，跑完了才输出结果
            long end = System.currentTimeMillis();

            // TODO: 2022/11/7 封装脚本中的错误该如何处理
            // currentMsrIns = msInsService.getCurrentMsrIns(msInsId);

            // exitVal == 0 为程序执行成功
            // exitVal == 1 为程序异常终止
            // exitVal == -1 为程序执行成功, 但自定义返回错误代码
            if (exitVal == 0) {
                log.info("Exec done, cost: " + ((end - start) / 1000) + "s");
                log.info("[程序正常退出] " + response);
            } else if (exitVal == -1) {
                String msg = "[程序自定义错误] " + response;
                log.error(msg);
                execError(ins, msg);
            } else {
                String msg = "[程序内部错误] " + error;
                log.error(msg);
                execError(ins, msg);
            }
        } catch (Exception e) {
            String msg = "[执行终端命令出错] " + e.getMessage();
            log.error(msg);
            execError(ins, msg);
            // if (currentMsrIns != null){
                // currentMsrIns.getLogs().add(new Log("执行终端命令出错: " + e.getMessage()));
            // }
        }

    }

    // @Async
    @Override
    public void pkgDispatcher(ModelService modelService) {

        // 把压缩包解压到 /service 文件夹下
        File zipFile = new File(repository + modelService.getDeployPkgPath());
        String outFilePathStr = repository + "/service/" + FileNameUtil.mainName(zipFile);
        File outFileDir = new File(outFilePathStr);
        ZipUtil.unzip(zipFile, outFileDir);

        // 将镜像导入到docker中
        String inputPath = outFilePathStr + "/" + modelService.getImageTar();
        int cnt = dockerOperMapper.countImageByRepository(modelService.getMsName());
        String tag = String.valueOf((cnt + 1)) + ".0";
        TerminalRes res = dockerService.importContainer(inputPath, modelService.getMsName() + ":" + tag);
        if (TerminalRes.SUCCESS_CODE.equals(res.getCode())){
            modelService.setDeployStatus(true);
            modelService.setImageName(modelService.getMsName() + ":" + tag);
            // 重置模型服务关联的容器id
            modelService.setContainerId(null);
            modelServiceMapper.updateModelService(modelService);

            ImageInfo imageInfo = new ImageInfo();
            imageInfo.setId(SnowFlake.nextId());
            imageInfo.setImageId(res.getMsg());
            imageInfo.setRepository(modelService.getMsName());
            imageInfo.setTag(tag);
            dockerOperMapper.insertImage(imageInfo);

            // 删除镜像包
            FileUtil.del(inputPath);
        }

    }

    @Async
    @Override
    public void createNewEnv(ModelService modelService) {
        // 根据当前环境生成镜像包
        JupyterInfoDTO container = workspaceService.getJupyterContainerById(modelService.getContainerId());
        if (container == null){
            throw new ServiceException("未找到对应的容器");
        }
        // 导出镜像的uuid
        String pkgUUID = UUID.fastUUID().toString();
        String imgOutName = pkgUUID + ".tar";
        TerminalRes terminalRes = dockerService.exportContainer(container.getContainerInsId(),
            repository + ContainerConstants.IMAGE_PATH + "/" + imgOutName);
        if (terminalRes.getCode().equals(TerminalRes.ERROR_CODE)){
            throw new ServiceException("导出镜像出错");
        }

        // 压缩打包
        String zipFile = ContainerConstants.PACKAGE_PATH + "/" + modelService.getMsName() + "_" + pkgUUID + ".zip";
        ZipUtil.zip(
            new File(repository + zipFile),
            true,
            new File(repository + "/workspace/" + modelService.getContainerId() + "/config"),
            new File(repository + ContainerConstants.serviceDir(modelService.getContainerId()) + "/" + modelService.getRelativeDir() + "/model"),
            new File(repository + ContainerConstants.IMAGE_PATH + "/" + imgOutName));

        // 打包后删除该镜像tar包
        FileUtil.del(new File(repository + ContainerConstants.IMAGE_PATH + "/" + pkgUUID + ".tar"));

        // TODO: 2022/12/15 把生成的部署包发送给所有计算节点
        modelService.setNewPkgId(pkgUUID);
        modelService.setRelativeDir("");
        modelService.setDeployPkgPath(zipFile);
        modelService.setImageTar(imgOutName);
        modelServiceMapper.updateModelService(modelService);
        // 部署包分发器
        pkgDispatcher(modelService);


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
            ProcessStatus.ERROR,
            msg,
            new Date()
        ));

        if (ins.getStartTime() == null){
            ins.setSpanTime(0);
        } else {
            int second = DateUtils.differentSecondsByMillisecond(new Date(), ins.getStartTime());
            ins.setSpanTime(second);
        }
        msrInsMapper.updateMsrIns(ins);

        msInsService.removeChannelAndMsrInsColl(ins.getSocketChannel());

    }
}
