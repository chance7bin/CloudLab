package org.opengms.container.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 容器内部常量
 *
 * @author 7bin
 * @date 2022/12/02
 */
public class ContainerConstants {


    // ======  容器内目录 ===========
    /** 容器内部jupyter配置文件目录 */
    public static final String INNER_CONFIG_PATH = "/jupyter_lab_config.py";

    /** 容器内部jupyter工作空间目录 */
    public static final String INNER_WORKSPACE_DIR = "/opt/notebooks";

    /** 容器内部模型服务目录 */
    public static final String INNER_SERVICE_DIR = "/opt/service";


    // ======  宿主机目录 ===========
    /** 工作空间的jupyter配置文件存放在 container.repository 目录的pod/{containerId}/config文件夹下 */
    public static String configDir(Long containerId){
        return "/workspace/" + containerId + "/config";
    }

    /** 工作空间的用户文件存放在 container.repository 目录的pod/{containerId}/data文件夹下 */
    public static String workspaceDir(Long containerId){
        return "/workspace/" + containerId + "/data";
    }

    /** 该工作空间所在的容器创建的模型服务都在 container.repository 目录的 workspace/{containerId}/service下 */
    public static String serviceDir(Long containerId){
        return "/workspace/" + containerId + "/service";
    }

    /** 启动jupyter命令 */
    // public static final String RUN_JUPYTER_CMD = "/bin/sh -c 'jupyter lab --config=/jupyter_lab_config.py'";
    public static final List<String> RUN_JUPYTER_CMD = new ArrayList<>(Arrays.asList("/bin/sh", "-c", "jupyter lab --config=/jupyter_lab_config.py --no-browser"));

    public static final List<String> RUN_DEFAULT_CMD = new ArrayList<>(Arrays.asList("tail", "-f", "/dev/null"));


    /** 生成的镜像包路径 */
    public static final String IMAGE_PATH = "/image";

    /** 生成的部署包路径 */
    public static final String PACKAGE_PATH = "/package";

}
