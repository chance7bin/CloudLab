package org.opengms.container.socket;

import lombok.extern.slf4j.Slf4j;
import org.opengms.container.service.ISocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * socket监听线程
 *
 * @author 7bin
 * @date 2022/10/18
 */
@Slf4j
@Component
public class SocketListener implements ApplicationRunner {

    @Autowired
    ISocketService socketService;

    @Override
    public void run(ApplicationArguments args) {
        socketService.startSocketListenerNIO();
    }
}
