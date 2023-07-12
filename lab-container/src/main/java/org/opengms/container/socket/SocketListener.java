package org.opengms.container.socket;

import javafx.beans.binding.MapExpression;
import lombok.extern.slf4j.Slf4j;
import org.opengms.container.service.INettyService;
import org.opengms.container.service.ISocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.Inet4Address;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * socket监听线程
 *
 * @author 7bin
 * @date 2022/10/18
 */
@Slf4j
@Component
public class SocketListener {

    @Autowired
    ISocketService socketService;

    @Autowired
    INettyService nettyService;

    private static final ThreadFactory springThreadFactory = new CustomizableThreadFactory("netty-pool-");
    private static final ExecutorService executors = Executors.newFixedThreadPool(1, springThreadFactory);


    @PostConstruct
    private void startListening() {
        executors.submit(() -> {
            nettyService.startSocketListener();
        });
    }

    // @Override
    // public void run(ApplicationArguments args) {
    //
    //     // TODO: 2023/4/25 当并发时会出现通信中断的问题
    //
    //     // socketService.startSocketListenerNIO();
    //
    //     // 使用netty替换原先的nio
    //     nettyService.startSocketListener();
    //
    // }
}
