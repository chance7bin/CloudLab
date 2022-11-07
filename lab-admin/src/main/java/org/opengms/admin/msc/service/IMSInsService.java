package org.opengms.admin.msc.service;

import org.opengms.admin.msc.entity.bo.MsrIns;
import org.opengms.admin.msc.entity.socket.Client;
import org.opengms.admin.msc.enums.ProcessState;

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

    void kill(String msg);

    void kill(SocketChannel channel, String msg);

    void kill(String msg, ProcessState state);

    Map<String, Client> getClients();

    Map<String, MsrIns> getMsrInsColl();

    // Client getConnectingChannel(SocketChannel clientChannel);

    MsrIns getCurrentMsrIns(SocketChannel clientChannel);

    MsrIns getCurrentMsrIns(String msInsId);

    void removeSocketChannel(SocketChannel clientChannel);

    // void initialize(String id);
    //
    // void enterState(String id, String state);
    //
    // void fireEvent(String id, String state, String event);
    //
    // void requestData(String id, String state, String event);
    //
    // void responseData(String id, String state, String event,
    //                   String data, String dataSignal, String dataType, String dataFormat);
    //
    // void postErrorInfo(String id, String errorInfo);
    //
    // void postWarningInfo(String id, String warningInfo);
    //
    // void postMessageInfo(String id, String messageInfo);
    //
    // void leaveState(String id, String state);
    //
    // //encapsulation 完成 (finalize是object接口的方法)
    // void eFinalize(String id);
}
