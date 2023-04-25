package org.opengms.container.handler.socket;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.opengms.container.service.IMSInsSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 状态选择器，用于确定接收到的消息应该执行哪个步骤的方法
 *
 * @author 7bin
 * @date 2023/04/25
 */
@Slf4j
@ChannelHandler.Sharable
@Component
public class StateSelectorHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    IMSInsSocketService msInsSocketService;

    private static StateSelectorHandler stateSelectorHandler;

    @PostConstruct
    public void init() {
        stateSelectorHandler = this;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // netty中无法使用注入的bean，因为NettyServerHandler是netty启动的时候new出来，并没有交给spring IOC托管
        // log.info("receive {} msg: {}", ctx.channel().remoteAddress(), msg);
        stateSelectorHandler.msInsSocketService.stateSelector(ctx.channel(), (String) msg);
    }
}


