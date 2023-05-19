package org.opengms.container.service;

import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.Container;
import org.opengms.common.TerminalRes;
import org.opengms.container.entity.bo.ExecResponse;
import org.opengms.container.entity.dto.docker.ContainerInfoDTO;
import org.opengms.container.entity.dto.docker.ImageInfoDTO;
import org.opengms.container.entity.dto.docker.JupyterInfoDTO;
import org.opengms.container.entity.po.docker.ContainerInfo;
import org.opengms.container.entity.po.docker.ImageInfo;

import java.io.IOException;
import java.util.List;

/**
 * 直接与docker交互的操作
 *
 * @author bin
 * @date 2022/10/5
 */
public interface IDockerService extends IAbstractContainerService{

    void pushImage(String imageName, String tag) throws InterruptedException;

    void pullImage(String imageName, String tag) throws InterruptedException;

    ExecResponse exec(String[] cmdArr) throws IOException, InterruptedException;


    /**
     * 得到真正的镜像名（是否附带docker hub 地址前缀）
     * @param imageNameWithTag
     * @return {@link String}
     * @author 7bin
     **/
    String getRealImageNameWithTag(String imageNameWithTag);
}
