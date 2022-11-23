package org.opengms.container.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.opengms.container.entity.bo.MsrIns;
import org.opengms.container.service.IMSCAsyncService;
import org.opengms.container.service.IMSInsService;
import org.opengms.common.utils.TerminalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @author 7bin
 * @date 2022/11/07
 */
@Slf4j
@Service
public class MSCAsyncServiceImpl implements IMSCAsyncService {

    @Autowired
    IMSInsService msInsService;

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

        String msInsId = cmdArr[cmdArr.length - 1];

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
                // System.out.println(response);
                // currentMsrIns.getLogs().add(new Log("程序执行完成, 用时: " + ((end - start) / 1000) + "s"));
            } else if (exitVal == -1) {
                log.error("[程序自定义错误] " + response);
                // currentMsrIns.getLogs().add(new Log("程序自定义错误: " + response));
            } else {
                log.error("[程序内部错误] " + error);
                // currentMsrIns.getLogs().add(new Log("程序内部错误: " + error));
            }
        } catch (Exception e) {
            log.error("[执行终端命令出错] " + e.getMessage());
            // if (currentMsrIns != null){
                // currentMsrIns.getLogs().add(new Log("执行终端命令出错: " + e.getMessage()));
            // }
        }

    }
}
