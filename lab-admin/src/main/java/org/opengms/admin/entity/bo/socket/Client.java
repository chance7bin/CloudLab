package org.opengms.admin.entity.bo.socket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.channels.SocketChannel;

/**
 * socket 客户端实体类
 *
 * @author 7bin
 * @date 2022/10/19
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Client {

    /** 客户端标识 */
    String clientId;

    /** 客户端channel */
    SocketChannel socketChannel;

    /** 接收到的消息 */
    String recvMessage;

    /** 发送给客户端的消息 */
    String sendMessage;

}
