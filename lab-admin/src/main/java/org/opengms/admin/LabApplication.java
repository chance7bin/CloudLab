package org.opengms.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Description 启动程序
 * @author bin
 * @date 2022/08/22
 */

@SpringBootApplication()
public class LabApplication {
    public static void main(String[] args)
    {
        SpringApplication.run(LabApplication.class, args);
    }
}
