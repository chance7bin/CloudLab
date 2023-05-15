package org.opengms.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.opengms.common.TerminalRes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author 7bin
 * @date 2022/11/04
 */
@Slf4j
public class TerminalUtils {

    private static String errorMsg;

    public static String getErrorMsg() {
        return errorMsg;
    }

    public static void setErrorMsg(String errorMsg) {
        TerminalUtils.errorMsg = errorMsg;
    }

    /**
     * 执行终端命令
     *
     * @param cmdArr 执行命令
     * @return long 退出状态码 0:[成功]; -1 or 其他:[失败]
     * @author 7bin
     **/
    public static TerminalRes exec(String[] cmdArr) {

        long start = System.currentTimeMillis();
        // String exe = "python";
        // 在window下用\表示路径，而在linux都是用/表示路径。在有路径需要修改的时候，要注意区分
        // String command = "E:\\opengms-lab\\container\\workspace\\jupyter_cus_5.0_8268889755334766592\\encapsulation.py";
        // String[] cmdArr = new String[] {exe, command, p1, p2, p3, p4, p5, p6, p7, p8, p9};
        log.info("Exec cmd: {}", Arrays.toString(cmdArr));
        // log.info("Exec cmd: {}", command);
        try {
            //这个方法是类似隐形开启了命令执行器，输入指令执行python脚本
            Process process = Runtime.getRuntime()
                .exec(cmdArr); // "python解释器位置（这里一定要用python解释器所在位置不要用python这个指令）+ python脚本所在路径（一定绝对路径）"

            String response = getInputMsg(process);
            String error = getErrorMsg(process);

            int exitVal = process.waitFor(); // 阻塞程序，跑完了才输出结果
            long end = System.currentTimeMillis();

            // exitVal == 0 为程序执行成功
            // exitVal == 1 为程序异常终止
            // exitVal == -1 为程序执行成功, 但自定义返回错误代码
            // if (exitVal == 0) {
            //     log.info("Exec done, cost: " + ((end - start) / 1000) + "s");
            // } else if (exitVal == -1){
            //     log.error("程序自定义错误:\n " + response);
            //     setErrorMsg(response);
            // } else {
            //     log.error("执行终端命令出错:\n " + error);
            //     setErrorMsg(error);
            // }

            if (exitVal == 0) {
                return TerminalRes.success(response);
            } else {
                // exitVal = 1
                return TerminalRes.error(error);
            }
        } catch (Exception e) {
            // log.error("执行终端命令出错:\n " + e.getMessage());
            // setErrorMsg(e.getMessage());
            // return new TerminalRes(-1, "Terminal command is wrong: " + e.getMessage());
            return TerminalRes.error("Terminal command is wrong: " + e.getMessage());
        }

    }

    // public static long exec(String[] cmdArr) {
    //     String command = Arrays.toString(cmdArr);
    //     return exec(command);
    // }

    // 获取子进程的错误信息
    // public static String getErrorMsg(Process process) throws IOException {
    //     // 采用字节流读取缓冲池内容，腾出空间
    //     ByteArrayOutputStream pool = new ByteArrayOutputStream();
    //     byte[] buffer = new byte[1024];
    //     int count = -1;
    //     while ((count = process.getErrorStream().read(buffer)) != -1) {
    //         pool.write(buffer, 0, count);
    //         buffer = new byte[1024];
    //     }
    //     return pool.toString("gbk").trim();
    // }

    // 获取子进程的输入信息
    // public static String getInputMsg(Process process) throws IOException {
    //     // 采用字节流读取缓冲池内容，腾出空间
    //     ByteArrayOutputStream pool = new ByteArrayOutputStream();
    //     byte[] buffer = new byte[1024];
    //     int count = -1;
    //     while ((count = process.getInputStream().read(buffer)) != -1) {
    //         pool.write(buffer, 0, count);
    //         buffer = new byte[1024];
    //     }
    //     return pool.toString("gbk").trim();
    // }

    // 获取子进程的输入信息
    public static String getInputMsg(Process process) throws IOException {

        // 检查进程是否已经启动
        if (!process.isAlive()) {
            // 进程已经终止
            return "";
        }

        // 获取输入流和输出流
        final InputStream stdout = process.getInputStream();
        // final InputStream stderr = process.getErrorStream();


        return readStream(stdout);
    }

    // 获取子进程的错误信息
    public static String getErrorMsg(Process process) throws IOException {

        // 检查进程是否已经启动
        if (!process.isAlive()) {
            // 进程已经终止
            return "";
        }

        // 获取输入流和输出流
        // final InputStream stdout = process.getInputStream();
        final InputStream stderr = process.getErrorStream();

        return readStream(stderr);

    }

    // 读取输入流，并将内容转换成字符串
    private static String readStream(InputStream inputStream) throws IOException {


        // 采用字节流读取缓冲池内容，腾出空间
        ByteArrayOutputStream pool = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count = -1;
        while ((count = inputStream.read(buffer)) != -1) {
            pool.write(buffer, 0, count);
            buffer = new byte[1024];
        }

        // 关闭流
        inputStream.close();

        return pool.toString("utf-8").trim();
    }


}
