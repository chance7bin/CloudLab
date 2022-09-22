package org.opengms.admin;

import org.junit.jupiter.api.Test;
import org.opengms.admin.config.AppConfig;
import org.opengms.admin.entity.bo.Server;
import org.opengms.common.utils.file.FileUtils;
import org.opengms.common.utils.ip.IpUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.*;

/**
 * @author bin
 * @date 2022/09/22
 */
@SpringBootTest
public class LabApplication {

    // 生成jupyter配置文件
    @Test
    void generateJupyterConfig() throws IOException {

        String resourcePath = "static/jupyter_lab_config.py";

        String s = FileUtils.readResourceTxtFile(resourcePath);

        System.out.println(s);

        s += "c.ServerApp.token = '66666'";

        FileUtils.writeBytes(s.getBytes(), "E:","jupyter_lab_config.py");

    }

    // 得到服务器信息
    @Test
    void getOSInfo() throws Exception {
        Server server = new Server();
        server.copyTo();
        System.out.println(server);

    }

    // 得到mac地址
    @Test
    void getMac(){
        System.out.println(IpUtils.getMacAddress());

    }

}
