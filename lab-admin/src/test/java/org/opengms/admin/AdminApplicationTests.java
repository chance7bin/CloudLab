package org.opengms.admin;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.opengms.admin.entity.bo.Server;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author 7bin
 * @date 2022/11/17
 */
@SpringBootTest
@Slf4j
public class AdminApplicationTests {

    // 得到服务器信息
    @Test
    void getOSInfo() throws Exception {
        Server server = new Server();
        server.copyTo();
        System.out.println(server);

    }

}
