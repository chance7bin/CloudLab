package org.opengms.admin.config.socket;

import lombok.extern.slf4j.Slf4j;
import org.opengms.admin.service.IAsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
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
    IAsyncService asyncService;

    @Override
    public void run(ApplicationArguments args) {
        asyncService.startSocketListenerNIO();
    }
}
