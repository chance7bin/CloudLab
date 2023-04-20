package org.opengms.container.constant;

/**
 * @author 7bin
 * @date 2023/04/17
 */
public class ContainerStatus {

    /**
     * 正在运行
     */
    public static final String RUNNING = "running";

    /**
     * 暂停
     */
    public static final String PAUSED = "paused";


    /**
     * 停止/退出
     */
    public static final String EXITED = "exited";


    /**
     * 容器已经被创建，但是并未启动
     */
    public static final String CREATED = "created";

    /**
     * 重启中
     */
    public static final String RESTARTING = "restarting";

    /**
     * 已删除
     */
    public static final String DELETED = "deleted";

    /**
     * 其他状态
     */
    public static final String OTHER = "other";

}
