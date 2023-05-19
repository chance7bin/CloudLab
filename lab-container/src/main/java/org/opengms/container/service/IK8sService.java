package org.opengms.container.service;

import io.kubernetes.client.openapi.ApiException;
import org.opengms.container.entity.bo.ExecResponse;

import java.io.IOException;
import java.util.List;

/**
 * K8S接口
 *
 * @author 7bin
 * @date 2023/05/09
 */
public interface IK8sService extends IAbstractContainerService{

    ExecResponse exec(String namespace, String podName, String containerName, String[] command) throws ApiException, IOException, InterruptedException;

    void createPod(String podName, String namespace, String imageName, String tag);

    void createPod(String podName, String namespace, String imageNameWithTag);

    void createPod(String podName, String namespace, String imageNameWithTag, List<String> volumeList);

    void createPod(String podName, String namespace, String imageName, List<String> volumeList, List<String> command);

    void deletePod(String podName, String namespace);

    String getPodStatus(String podName, String namespace);

    String getPodContainerName(String podName);

}
