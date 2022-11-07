package org.opengms.admin.service.impl;

import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.opengms.admin.entity.po.system.SysLogininfor;
import org.opengms.admin.entity.po.system.SysOperLog;
import org.opengms.admin.service.IAsyncService;
import org.opengms.admin.service.ISysLogininforService;
import org.opengms.admin.service.ISysOperLogService;
import org.opengms.common.constant.Constants;
import org.opengms.common.utils.LogUtils;
import org.opengms.common.utils.ServletUtils;
import org.opengms.common.utils.StringUtils;
import org.opengms.common.utils.ip.AddressUtils;
import org.opengms.common.utils.ip.IpUtils;
import org.opengms.common.utils.uuid.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * @author bin
 * @date 2022/08/23
 */
@Slf4j
@Service
public class AsyncServiceImpl implements IAsyncService {

    @Autowired
    private ISysOperLogService sysOperLogService;

    @Autowired
    private ISysLogininforService sysLogininforService;


    /**
     * 操作日志记录
     *
     * @param operLog 操作日志信息
     * @return 任务task
     */
    @Async
    @Override
    public void recordOper(SysOperLog operLog) {

        // 远程查询操作地点
        operLog.setOperLocation(AddressUtils.getRealAddressByIP(operLog.getOperIp()));
        sysOperLogService.insertOperlog(operLog);

    }

    // 异步线程RequestContextHolder.getRequestAttributes()为null
    // @Async
    @Override
    public void recordLogininfor(String username, String status, String message, Object... args) {
        final UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        final String ip = IpUtils.getIpAddr(ServletUtils.getRequest());

        String address = AddressUtils.getRealAddressByIP(ip);
        StringBuilder s = new StringBuilder();
        s.append(LogUtils.getBlock(ip));
        s.append(address);
        s.append(LogUtils.getBlock(username));
        s.append(LogUtils.getBlock(status));
        s.append(LogUtils.getBlock(message));
        // 打印信息到日志
        log.info(s.toString(), args);
        // 获取客户端操作系统
        String os = userAgent.getOperatingSystem().getName();
        // 获取客户端浏览器
        String browser = userAgent.getBrowser().getName();
        // 封装对象
        SysLogininfor logininfor = new SysLogininfor();
        logininfor.setUserName(username);
        logininfor.setIpaddr(ip);
        logininfor.setLoginLocation(address);
        logininfor.setBrowser(browser);
        logininfor.setOs(os);
        logininfor.setMsg(message);
        // 日志状态
        if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER))
        {
            logininfor.setStatus(Constants.SUCCESS);
        }
        else if (Constants.LOGIN_FAIL.equals(status))
        {
            logininfor.setStatus(Constants.FAIL);
        }
        // 插入数据
        sysLogininforService.insertLogininfor(logininfor);


    }



}
