package org.opengms.container.constant;

/**
 * 服务类型
 *
 * @author 7bin
 * @date 2023/04/17
 */
public class ServiceType {

    /**
     * 只运行一次的服务，服务调用结束后立即销毁
     */
    public static final String DISPOSABLE = "0";


    /**
     * (默认)后台进程，容器启动后就在后台运行，不消毁
     */
    public static final String DAEMON = "1";

}
