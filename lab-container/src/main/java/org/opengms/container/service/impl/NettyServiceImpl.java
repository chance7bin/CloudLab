package org.opengms.container.service.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.opengms.container.handler.socket.QuitHandler;
import org.opengms.container.handler.socket.StateSelectorHandler;
import org.opengms.container.service.INettyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author 7bin
 * @date 2023/04/25
 */
@Slf4j
@Service
public class NettyServiceImpl implements INettyService {

    @Value("${socket.port}")
    private int socketPort;

    // socket传输的字节大小
    private final static int SOCKET_MESSAGE_SIZE = 1024;

    // @Async
    @Override
    public void startSocketListener() {

        log.info("SocketListener - netty server - listening on port : " + socketPort);

        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        StringDecoder STRING_DECODER = new StringDecoder();
        StringEncoder STRING_ENCODER = new StringEncoder();
        StateSelectorHandler STATE_SELECTOR_HANDLER = new StateSelectorHandler();
        QuitHandler QUIT_HANDLER = new QuitHandler();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    // 固定长度（定长解码器）
                    // ch.pipeline().addLast(new FixedLengthFrameDecoder(1024));
                    ch.pipeline().addLast(STRING_ENCODER);
                    ch.pipeline().addLast(STRING_DECODER);
                    ch.pipeline().addLast(STATE_SELECTOR_HANDLER);
                    ch.pipeline().addLast(QUIT_HANDLER);
                }
            });
            Channel channel = serverBootstrap.bind(socketPort).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("netty server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            log.error("netty server shutdown...");
        }

    }
}
