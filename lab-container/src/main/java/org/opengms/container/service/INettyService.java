package org.opengms.container.service;

/**
 * netty实现socket通信，替换掉原来的NIO通信 (并发时有问题)
 *
 * @author 7bin
 * @date 2023/04/25
 */
public interface INettyService {

    void startSocketListener();

}
