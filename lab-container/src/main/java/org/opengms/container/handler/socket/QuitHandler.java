package org.opengms.container.handler.socket;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.opengms.container.entity.po.MsrIns;
import org.opengms.container.service.IMSInsSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author 7bin
 * @date 2023/03/20
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class QuitHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    IMSInsSocketService msInsSocketService;

    private static QuitHandler quitHandler;

    @PostConstruct
    public void init() {
        quitHandler = this;
    }

    // 当连接断开时触发 inactive 事件
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("{} 已经断开", ctx.channel().remoteAddress());
        quitHandler.msInsSocketService.removeChannelAndMsrInsColl(ctx.channel());
        log.info("socket closed....... current connecting client number: " + quitHandler.msInsSocketService.getMsrInsColl().size());
    }

    // 当出现异常时触发
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("{} 断开异常 异常是{}", ctx.channel().remoteAddress() , cause.getMessage());
        quitHandler.msInsSocketService.removeChannelAndMsrInsColl(ctx.channel());
        log.info("socket closed....... current connecting client number: " + quitHandler.msInsSocketService.getMsrInsColl().size());
    }

}
