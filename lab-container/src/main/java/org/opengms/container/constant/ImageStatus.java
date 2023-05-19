package org.opengms.container.constant;

/**
 * 镜像状态
 *
 * @author 7bin
 * @date 2023/04/17
 */
public class ImageStatus {

    /**
     * 初始化
     */
    public static final String INIT = "init";

    /**
     * 已commit
     */
    public static final String COMMITTED = "committed";

    /**
     * 已push
     */
    public static final String PUSHED = "pushed";

    /**
     * 已完成
     */
    public static final String FINISHED = "finished";

    /**
     * 失败
     */
    public static final String ERROR = "error";

    /**
     * 其他状态
     */
    public static final String OTHER = "other";

}
