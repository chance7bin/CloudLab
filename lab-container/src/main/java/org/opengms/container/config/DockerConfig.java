package org.opengms.container.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * docker配置信息
 *
 * @author bin
 * @date 2022/10/12
 */
@Slf4j
@Configuration
public class DockerConfig {

    @Value("${docker.clientHost}")
    private String clientHost;

    @Value("${docker.clientPort}")
    private String clientPort;

    @Bean(name = "dockerClient")
    DockerClient dockerClient(){
        return connect();
    }

    /** 连接docker */
    private DockerClient connect() {
        String host = "tcp://" + clientHost + ":" + clientPort;
        // String apiVersion = "1.38";
        //创建DefaultDockerClientConfig
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost(host)
            // .withApiVersion(apiVersion)
            // .withDockerHost(host)
            .build();
        //创建DockerHttpClient
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .maxConnections(100)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build();
        //创建DockerClient
        DockerClient client = DockerClientImpl.getInstance(config, httpClient);

        log.info("docker initialize successfully!");

        Info info = client.infoCmd().exec();
        // log.info("docker info : " + info.toString());

        return client;
    }

}
