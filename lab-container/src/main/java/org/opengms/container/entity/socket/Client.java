package org.opengms.container.entity.socket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengms.container.entity.po.MsrIns;

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

    /** 模型运行实例 */
    MsrIns msrIns;

}
