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
    /** 工作空间的jupyter配置文件存放在 ${container.repository} 目录的 /workspace/{containerId}/config 文件夹下 */
    public static String CONFIG_DIR(Long containerId){
        return "/workspace/" + containerId + "/config";
    }

    /** 工作空间的用户文件存放在 container.repository 目录的 /workspace/{containerId}/data 文件夹下 */
    public static String DATA_DIR(Long containerId){
        return "/workspace/" + containerId + "/data";
    }

    /** (Deprecated)该工作空间所在的容器创建的模型服务都在 container.repository 目录的 /workspace/{containerId}/service 文件夹下 */
    // (Deprecated)统一放在了 repository/service/{containerId}下
    // container 不挂载 service 目录了
    // 文件直接拷贝到 /service/{msId} 下
    public static String SERVICE_DIR(Long msId){
        return "/service/" + msId;
    }

    /** 启动jupyter命令 */
    // public static final String RUN_JUPYTER_CMD = "/bin/sh -c 'jupyter lab --config=/jupyter_lab_config.py'";
    public static final List<String> RUN_JUPYTER_CMD = new ArrayList<>(Arrays.asList("/bin/sh", "-c", "jupyter lab --config=/jupyter_lab_config.py --no-browser"));

    public static final List<String> RUN_DEFAULT_CMD = new ArrayList<>(Arrays.asList("tail", "-f", "/dev/null"));


    /** 生成的镜像包路径 */
    public static final String IMAGE_PATH = "/image";

    /** 生成的部署包文件夹 */
    public static final String PACKAGE_DIR = "/package";

    /** 生成的部署包路径 */
    public static String PACKAGE_PATH(String pkgId){
        return PACKAGE_DIR + "/" + "pkg_" + pkgId + ".zip";
    }

}
