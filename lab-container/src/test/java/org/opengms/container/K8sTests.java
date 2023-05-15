package org.opengms.container;

import io.kubernetes.client.Exec;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.auth.ApiKeyAuth;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.opengms.common.utils.TerminalUtils;
import org.opengms.container.constant.ContainerConstants;
import org.opengms.container.service.IAbstractContainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;

import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * k8s测试代码
 *
 * @author 7bin
 * @date 2023/04/26
 */
@Slf4j
@SpringBootTest
public class K8sTests {

    @Value(value = "${container.repository}")
    String repository;

    @Value(value = "${docker.dockerRegistryUrl}")
    String dockerRegistryUrl;

    // @Autowired
    // @Qualifier(value = "k8sClient")
    // ApiClient k8sClient;

    @Autowired
    @Qualifier(value = "k8sApi")
    CoreV1Api k8sApi;

    @Autowired
    @Qualifier(value = "k8sServiceImpl")
    IAbstractContainerService k8sService;

    @Test
    void k8sClientHelloWorld() throws IOException, ApiException {

        // file path to your KubeConfig
        String kubeConfigPath = repository + "/resource/kube/config";

        // loading the out-of-cluster config, a kubeconfig from file-system
        ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();

        // set the global default api-client to the in-cluster one from above
        Configuration.setDefaultApiClient(client);

        // the CoreV1Api loads default api-client from global configuration.
        CoreV1Api api = new CoreV1Api();

        // invokes the CoreV1Api client
        V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, "true", null, null, null);
        System.out.println("Listing all pods: ");
        for (V1Pod item : list.getItems()) {
            System.out.println(item.getMetadata().getName());
        }
    }




    @Test
    void k8sClientTest(){


        String namespace = "dev"; // String | object name and auth scope, such as for teams and projects
        String pretty = "true"; // String | If 'true', then the output is pretty printed.
        Boolean allowWatchBookmarks = true; // Boolean | allowWatchBookmarks requests watch events with type \"BOOKMARK\". Servers that do not implement bookmarks may ignore this flag and bookmarks are sent at the server's discretion. Clients should not assume bookmarks are returned at any specific interval, nor may they assume the server will send any BOOKMARK event during a session. If this is not a watch, this field is ignored.
        String _continue = null; // String | The continue option should be set when retrieving more results from the server. Since this value is server defined, clients may only use the continue value from a previous query result with identical query parameters (except for the value of continue) and the server may reject a continue value it does not recognize. If the specified continue value is no longer valid whether due to expiration (generally five to fifteen minutes) or a configuration change on the server, the server will respond with a 410 ResourceExpired error together with a continue token. If the client needs a consistent list, it must restart their list without the continue field. Otherwise, the client may send another list request with the token received with the 410 error, the server will respond with a list starting from the next key, but from the latest snapshot, which is inconsistent from the previous list results - objects that are created, modified, or deleted after the first list request will be included in the response, as long as their keys are after the \"next key\".  This field is not supported when watch is true. Clients may start a watch from the last resourceVersion value returned by the server and not miss any modifications.
        String fieldSelector = null; // String | A selector to restrict the list of returned objects by their fields. Defaults to everything.
        String labelSelector = null; // String | A selector to restrict the list of returned objects by their labels. Defaults to everything.
        Integer limit = 56; // Integer | limit is a maximum number of responses to return for a list call. If more items exist, the server will set the `continue` field on the list metadata to a value that can be used with the same initial query to retrieve the next set of results. Setting a limit may return fewer than the requested amount of items (up to zero items) in the event all requested objects are filtered out and clients should only use the presence of the continue field to determine whether more results are available. Servers may choose not to support the limit argument and will return all of the available results. If limit is specified and the continue field is empty, clients may assume that no more results are available. This field is not supported if watch is true.  The server guarantees that the objects returned when using continue will be identical to issuing a single list call without a limit - that is, no objects created, modified, or deleted after the first request is issued will be included in any subsequent continued requests. This is sometimes referred to as a consistent snapshot, and ensures that a client that is using limit to receive smaller chunks of a very large result can ensure they see all possible objects. If objects are updated during a chunked list the version of the object that was present at the time the first list result was calculated is returned.
        String resourceVersion = null; // String | resourceVersion sets a constraint on what resource versions a request may be served from. See https://kubernetes.io/docs/reference/using-api/api-concepts/#resource-versions for details.  Defaults to unset
        String resourceVersionMatch = null; // String | resourceVersionMatch determines how resourceVersion is applied to list calls. It is highly recommended that resourceVersionMatch be set for list calls where resourceVersion is set See https://kubernetes.io/docs/reference/using-api/api-concepts/#resource-versions for details.  Defaults to unset
        Integer timeoutSeconds = null; // Integer | Timeout for the list/watch call. This limits the duration of the call, regardless of any activity or inactivity.
        Boolean watch = null; // Boolean | Watch for changes to the described resources and return them as a stream of add, update, and remove notifications. Specify resourceVersion.
        try {
            V1PodList result = k8sApi.listNamespacedPod(namespace, pretty, allowWatchBookmarks, _continue, fieldSelector, labelSelector, limit, resourceVersion, timeoutSeconds, watch);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling CoreV1Api#listNamespacedPod");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }

    }

    @Test
    void testK8sService(){
        k8sService.createContainer(null);
    }

    @Test
    void testCreatePod() {

        V1Pod pod = new V1Pod();
        pod.setApiVersion("v1");
        pod.setKind("Pod");
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName("ms-test");
        // metadata.setNamespace("dev");
        pod.setMetadata(metadata);

        V1PodSpec spec = new V1PodSpec();

        Long msId = 8344476353811312640L;
        String serviceDir = ContainerConstants.SERVICE_DIR(msId);

        V1Container container = new V1Container();
        container.setName("newimg1");
        container.setImage(dockerRegistryUrl + "/newimg1:1.0");
        container.setPorts(Arrays.asList(new V1ContainerPort().containerPort(80)));
        container.setVolumeMounts(Arrays.asList(new V1VolumeMount().mountPath(ContainerConstants.INNER_SERVICE_DIR).name("service-volume")));
        container.setImagePullPolicy("IfNotPresent");
        container.setCommand(ContainerConstants.RUN_DEFAULT_CMD);
        spec.setContainers(Arrays.asList(container));

        V1Volume volume = new V1Volume();
        volume.setName("service-volume");
        volume.setNfs(new V1NFSVolumeSource().path("/e/opengms-lab/container" + serviceDir).server("172.21.212.240"));
        spec.setVolumes(Arrays.asList(volume));

        pod.setSpec(spec);

        try {
            k8sApi.createNamespacedPod("dev", pod, null, null, null);
            // System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling CoreV1Api#createNamespacedPod");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            // System.err.println("Response headers: " + e.getResponseHeaders());
            // e.printStackTrace();
        }



    }

    @Test
    void testAutowired(){
        System.out.println();
    }

    @Test
    void testDeletePod() {

        try {

            // 创建 V1DeleteOptions 对象，用于指定删除 Pod 的参数
            V1DeleteOptions deleteOptions = new V1DeleteOptions();
            // deleteOptions.setGracePeriodSeconds(0L);
            // deleteOptions.setPropagationPolicy("Foreground");
            k8sApi.deleteNamespacedPod("ms-test", "dev", null, null, null, null, null, deleteOptions);
            // System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling CoreV1Api#deleteNamespacedPod");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            // System.err.println("Response headers: " + e.getResponseHeaders());
            // e.printStackTrace();
        }


    }

    @Test
    void testReadPod(){

        String name = "client-pod2"; // String | name of the Pod
        String namespace = "dev"; // String | object name and auth scope, such as for teams and projects
        String pretty = "true"; // String | If 'true', then the output is pretty printed.
        Boolean exact = true; // Boolean | Should the export be exact.  Exact export maintains cluster-specific fields like 'Namespace'. Deprecated. Planned for removal in 1.18.
        Boolean export = true; // Boolean | Should this value be exported.  Export strips fields that a user can not specify. Deprecated. Planned for removal in 1.18.
        try {
            V1Pod result = k8sApi.readNamespacedPodStatus(name, namespace, pretty);
            String phase = result.getStatus().getPhase();
            String hostIP = result.getStatus().getHostIP();
            System.out.println(hostIP + " " + phase);
            // System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling CoreV1Api#readNamespacedPod");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }


    @Test
    void testExec() throws Exception {

        String namespace = "dev";
        String podName = "ms-test";
        String containerName = "newimg1";

        // 获取 Pod 对象
        V1Pod pod = k8sApi.readNamespacedPod(podName, namespace, null, null, null);

        // 获取容器对象
        V1Container container = pod.getSpec().getContainers().stream()
            .filter(c -> c.getName().equals(containerName))
            .findFirst().orElse(null);
        if (container == null) {
            throw new Exception("Container not found");
        }

        // Command to be executed
        List<String> command = Arrays.asList("python", "/opt/service/我是谁？/test.py");
        // list 转 array
        String[] commandArray = command.toArray(new String[command.size()]);

        // Create an instance of Exec
        Exec exec = new Exec();

        // Exec the command
        final Process process = exec.exec(namespace, podName, commandArray, containerName , Boolean.TRUE.equals(container.getStdin()), Boolean.TRUE.equals(container.getTty()));

        // 获取输入流和输出流
        String response = getInputMsg(process);
        String error = getErrorMsg(process);

        // 获取输入流和输出流
        // final InputStream stdout = process.getInputStream();
        // final InputStream stderr = process.getErrorStream();

        // 等待命令执行完成
        // boolean exitCode = process.waitFor(60, TimeUnit.SECONDS);

        // 打印输出流
        // byte[] buf = new byte[1024];
        // int read;
        // while ((read = stdout.read(buf)) > 0) {
        //     System.out.print(new String(buf, 0, read));
        // }
        // while ((read = stderr.read(buf)) > 0) {
        //     System.err.print(new String(buf, 0, read));
        // }

        // 关闭流
        // stdout.close();
        // stderr.close();

        // 等待命令执行完成
        int exitCode = process.waitFor();


        // 打印退出码
        System.out.println("Exit code: " + exitCode);
        System.out.println("response: " + response);
        System.out.println("error: " + error);




    }

    // 获取子进程的输入信息
    public static String getInputMsg(Process process) throws IOException {

        // 检查进程是否已经启动
        if (!process.isAlive()) {
            // 进程已经终止
            return "";
        }

        // 获取输入流和输出流
        final InputStream stdout = process.getInputStream();
        // final InputStream stderr = process.getErrorStream();

        // 采用字节流读取缓冲池内容，腾出空间
        // ByteArrayOutputStream pool = new ByteArrayOutputStream();
        // byte[] buffer = new byte[1024];
        // int count = -1;
        // while ((count = stdout.read(buffer)) != -1) {
        //     pool.write(buffer, 0, count);
        //     buffer = new byte[1024];
        // }
        //
        // // 关闭流
        // stdout.close();
        // // stderr.close();
        //
        // return pool.toString("utf-8").trim();

        return readStream(stdout);
    }

    // 获取子进程的错误信息
    public static String getErrorMsg(Process process) throws IOException {

        // 检查进程是否已经启动
        if (!process.isAlive()) {
            // 进程已经终止
            return "";
        }

        // 获取输入流和输出流
        // final InputStream stdout = process.getInputStream();
        final InputStream stderr = process.getErrorStream();

        return readStream(stderr);

        // 采用字节流读取缓冲池内容，腾出空间
        // ByteArrayOutputStream pool = new ByteArrayOutputStream();
        // byte[] buffer = new byte[1024];
        // int count = -1;
        // while ((count = stderr.read(buffer)) != -1) {
        //     pool.write(buffer, 0, count);
        //     buffer = new byte[1024];
        // }
        //
        // // 关闭流
        // // stdout.close();
        // stderr.close();
        //
        // return pool.toString("utf-8").trim();
    }

    // 读取输入流，并将内容转换成字符串
    private static String readStream(InputStream inputStream) throws IOException {
        // BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        // StringBuilder result = new StringBuilder();
        // String line;
        // while ((line = reader.readLine()) != null) {
        //     result.append(line).append("\n");
        // }
        //
        //
        //
        // return result.toString();

        // 采用字节流读取缓冲池内容，腾出空间
        ByteArrayOutputStream pool = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count = -1;
        while ((count = inputStream.read(buffer)) != -1) {
            pool.write(buffer, 0, count);
            buffer = new byte[1024];
        }

        // 关闭流
        // stdout.close();
        inputStream.close();

        return pool.toString("utf-8").trim();
    }


}
