package org.opengms.container.service.impl;

import io.kubernetes.client.Exec;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.opengms.common.TerminalRes;
import org.opengms.common.utils.TerminalUtils;
import org.opengms.container.config.K8sConfig;
import org.opengms.container.constant.ContainerConstants;
import org.opengms.container.entity.bo.ExecResponse;
import org.opengms.container.entity.po.docker.ContainerInfo;
import org.opengms.container.entity.po.docker.ImageInfo;
import org.opengms.container.exception.ServiceException;
import org.opengms.container.service.IAbstractContainerService;
import org.opengms.container.service.IK8sService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 直接与k8s交互的操作 实现类
 *
 * @author 7bin
 * @date 2023/04/26
 */
@Slf4j
@Service
public class K8sServiceImpl implements IK8sService {

    @Autowired
    @Qualifier(value = "k8sApi")
    CoreV1Api k8sApi;


    @Value(value = "${docker.dockerRegistryUrl}")
    private String dockerRegistryUrl;

    @Value(value = "${nfs.host}")
    private String nfsHost;

    @Value(value = "${nfs.path}")
    private String nfsPath;


    @Override
    public ContainerInfo createContainer(ContainerInfo containerInfo) {
        System.out.println(123);
        return null;
    }

    @Override
    public int updateContainer(ContainerInfo containerInfo) {
        return 0;
    }

    @Override
    public String getContainerStatusByContainerInsId(String containerInsId) {
        return null;
    }

    @Override
    public ImageInfo inspectImage(String sha256) {
        return null;
    }

    @Override
    public List<ImageInfo> listImages() {
        return null;
    }

    @Override
    public List<ContainerInfo> listContainers() {
        return null;
    }

    @Override
    public ContainerInfo selectContainerByInsId(String insId) {
        return null;
    }

    @Override
    public void execCommand() {
    }

    @Override
    public String commitContainer(String containerInsId, String imageName, String tag) {
        return null;
    }

    @Override
    public void saveContainer() {

    }

    @Override
    public TerminalRes exportContainer(String container, String outputPath) {
        return null;
    }

    @Override
    public TerminalRes importContainer(String inputPath, String imageName) {
        return null;
    }

    @Override
    public void startContainer(String containerInsId) {

    }

    @Override
    public void stopContainer(String containerInsId) {

    }

    @Override
    public void removeContainer(String containerInsId) {

    }

    @Override
    public boolean isContainerRunning(String containerInsId) {
        return false;
    }


    @Override
    public ExecResponse exec(String namespace, String podName, String containerName, String[] command) throws ApiException, IOException, InterruptedException {

        // 获取 Pod 对象
        V1Pod pod = k8sApi.readNamespacedPod(podName, namespace, null, null, null);

        // 获取容器对象
        V1Container container = pod.getSpec().getContainers().stream()
            .filter(c -> c.getName().equals(containerName))
            .findFirst().orElse(null);
        if (container == null) {
            throw new ServiceException("Container not found: " + containerName);
        }

        // Create an instance of Exec
        Exec exec = new Exec();

        // Exec the command
        final Process process = exec.exec(namespace, podName, command, containerName , Boolean.TRUE.equals(container.getStdin()), Boolean.TRUE.equals(container.getTty()));

        // 获取输入流和输出流
        String response = TerminalUtils.getInputMsg(process);
        String error = TerminalUtils.getErrorMsg(process);

        // 等待命令执行完成
        int exitCode = process.waitFor();

        return new ExecResponse(exitCode, response, error);

    }

    @Override
    public void createPod(String podName, String namespace, String imageName, String tag) {

        V1Pod pod = new V1Pod();
        pod.setApiVersion("v1");
        pod.setKind("Pod");
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(podName);
        // metadata.setNamespace("dev");
        pod.setMetadata(metadata);

        V1PodSpec spec = new V1PodSpec();

        Long msId = 8344476353811312640L;
        String serviceDir = ContainerConstants.SERVICE_DIR(msId);

        V1Container container = new V1Container();

        container.setName(imageName);
        container.setImage(dockerRegistryUrl + "/" + imageName + ":" + tag);
        // container.setPorts(Arrays.asList(new V1ContainerPort().containerPort(80)));
        container.setVolumeMounts(Arrays.asList(new V1VolumeMount().mountPath(ContainerConstants.INNER_SERVICE_DIR).name("service-volume")));
        container.setImagePullPolicy("IfNotPresent");
        container.setCommand(ContainerConstants.RUN_DEFAULT_CMD);
        spec.setContainers(Arrays.asList(container));

        V1Volume volume = new V1Volume();
        volume.setName("service-volume");
        volume.setNfs(new V1NFSVolumeSource().path(nfsPath + serviceDir).server(nfsHost));
        spec.setVolumes(Arrays.asList(volume));

        pod.setSpec(spec);

        try {
            k8sApi.createNamespacedPod(namespace, pod, null, null, null);
        } catch (ApiException e) {
            log.error("Exception when calling CoreV1Api#createNamespacedPod");
            log.error("Status code: " + e.getCode());
            log.error("Reason: " + e.getResponseBody());
        }
    }

    @Override
    public void createPod(String podName, String namespace, String imageNameWithTag) {

        String[] split = imageNameWithTag.split(":");
        String imageName = split[0];
        String tag = split[1];
        createPod(podName, namespace, imageName, tag);

    }

    @Override
    public void deletePod(String podName, String namespace){
        try {
            // 创建 V1DeleteOptions 对象，用于指定删除 Pod 的参数
            V1DeleteOptions deleteOptions = new V1DeleteOptions();
            // deleteOptions.setGracePeriodSeconds(0L);
            // deleteOptions.setPropagationPolicy("Foreground");
            k8sApi.deleteNamespacedPod(podName, namespace, null, null, null, null, null, deleteOptions);
        } catch (ApiException e) {
            log.error("Exception when calling CoreV1Api#deleteNamespacedPod");
            log.error("Status code: " + e.getCode());
            log.error("Reason: " + e.getResponseBody());
        }
    }

    @Override
    public String getPodStatus(String podName, String namespace) {
        // String name = "ms-test"; // String | name of the Pod
        // String namespace = "dev"; // String | object name and auth scope, such as for teams and projects
        String pretty = "true"; // String | If 'true', then the output is pretty printed.
        Boolean exact = true; // Boolean | Should the export be exact.  Exact export maintains cluster-specific fields like 'Namespace'. Deprecated. Planned for removal in 1.18.
        Boolean export = true; // Boolean | Should this value be exported.  Export strips fields that a user can not specify. Deprecated. Planned for removal in 1.18.
        try {
            V1Pod result = k8sApi.readNamespacedPodStatus(podName, namespace, pretty);
            String phase = result.getStatus().getPhase();
            String hostIP = result.getStatus().getHostIP();
            return phase;
            // System.out.println(hostIP + " " + phase);
            // System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling CoreV1Api#readNamespacedPod");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            // System.err.println("Response headers: " + e.getResponseHeaders());
            // e.printStackTrace();
            return null;
        }
    }
}
