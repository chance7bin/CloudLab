package org.opengms.container.config;

import com.github.dockerjava.api.DockerClient;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import lombok.extern.slf4j.Slf4j;
import org.opengms.container.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;

import java.io.FileReader;
import java.io.IOException;

/**
 * java连接k8s的客户端
 *
 * @author 7bin
 * @date 2023/04/26
 */
@Slf4j
@org.springframework.context.annotation.Configuration
public class K8sConfig {

    @Value(value = "${container.repository}")
    private String repository;


    // @Bean(name = "k8sClient")
    // ApiClient k8sClient() throws IOException {
    //     return connect();
    // }

    @Bean(name = "k8sApi")
    CoreV1Api k8sApi() throws IOException {
        connect();
        return new CoreV1Api();
    }



    private ApiClient connect() throws IOException {
        // file path to your KubeConfig
        String kubeConfigPath = repository + "/resource/kube/config";

        // loading the out-of-cluster config, a kubeconfig from file-system
        ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();

        // set the global default api-client to the in-cluster one from above
        Configuration.setDefaultApiClient(client);

        // the CoreV1Api loads default api-client from global configuration.
        // CoreV1Api api = new CoreV1Api();
        // k8sApi = new CoreV1Api();

        log.info("kubernetes server has been connected.");

        return client;
    }

}
