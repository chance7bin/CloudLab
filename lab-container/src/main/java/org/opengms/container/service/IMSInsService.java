package org.opengms.container.service;

import org.opengms.container.entity.po.MsrIns;
import org.opengms.container.entity.socket.Client;
import org.opengms.container.enums.ProcessState;

import java.nio.channels.SocketChannel;
import java.util.Map;

/**
 * 模型容器实例接口
 *
 * @author 7bin
 * @date 2022/10/19
 */
public interface IMSInsService {

    // socket通信部分

    // 状态选择器，用于确定接收到的消息应该执行哪个步骤的方法
    void stateSelector(SocketChannel channel, String recvMessage);

    void stateSelector(SocketChannel channel);

    // void kill(String msg);

    void kill(SocketChannel channel, String msg, ProcessState processState);

    void kill(SocketChannel channel, String msg, ProcessState processState, String state, String event);

    void kill(String msg, ProcessState processState, String state, String event);

    void kill(String msg, ProcessState processState);

    Map<String, Client> getClients();

    Map<String, MsrIns> getMsrInsColl();

    // Client getConnectingChannel(SocketChannel clientChannel);

    MsrIns getCurrentMsrIns(SocketChannel clientChannel);

    MsrIns getCurrentMsrIns(String msInsId);

    void removeSocketChannel(SocketChannel clientChannel);

    MsrIns getMsrInsByMsriId(String msInsId);

    MsrIns getMsrInsFromMsrInsColl(String msriId);

    void removeChannelAndMsrInsColl(SocketChannel channel);

}
