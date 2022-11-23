package org.opengms.container.service;


/**
 * jupyter接口
 *
 * @author bin
 * @date 2022/10/6
 */
public interface IJupyterService {

    /**
     * 根据config的文件夹路径生成jupyter配置文件
     * @param configDir
     * @return 生成成功返回该配置文件路径，失败返回null
     * @Author bin
     **/
    String generateJupyterConfig(String configDir);

    /**
     * 根据config的文件夹路径生成jupyter配置文件
     * @param configDir 配置文件目录
     * @param suffix 配置文件后缀
     * @return 生成成功返回该配置文件路径，失败返回null
     * @Author bin
     **/
    String generateJupyterConfig(String configDir, String suffix);

    /**
     * 根据config的文件夹路径生成jupyter配置文件
     * @param configDir 配置文件目录
     * @param suffix 配置文件后缀
     * @param token jupyter登录token
     * @return 生成成功返回该配置文件路径，失败返回null
     * @Author bin
     **/
    String generateJupyterConfig(String configDir, String suffix, String token);
}
