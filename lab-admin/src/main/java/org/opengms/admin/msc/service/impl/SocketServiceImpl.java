package org.opengms.admin.msc.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.opengms.admin.exception.ServiceException;
import org.opengms.admin.msc.entity.socket.Client;
import org.opengms.admin.msc.service.IMSInsService;
import org.opengms.admin.msc.service.ISocketService;
import org.opengms.common.utils.uuid.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author 7bin
 * @date 2022/10/20
 */
@Service
@Slf4j
public class SocketServiceImpl implements ISocketService {

    @Autowired
    IMSInsService msInsService;

    @Value("${socket.port}")
    private int socketPort;

    // socket传输的字节大小
    private final static int SOCKET_MESSAGE_SIZE = 1024;

    @Async
    @Override
    public void startSocketListener() {
        log.info("SocketListener - listening on port : " + socketPort);


    }

    @Async
    @Override
    public void startSocketListenerNIO() {
        log.info("SocketListener - NIO - listening on port : " + socketPort);


        Selector selector = null;
        try {
            // 1. open a selector
            selector = Selector.open();
            // 2. listen for server socket channel
            ServerSocketChannel ssc = ServerSocketChannel.open();
            // must to be nonblocking mode before register
            ssc.configureBlocking(false);
            // bind server socket channel to port 8899
            ssc.bind(new InetSocketAddress(socketPort));
            // 3. register it with selector
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            while (true) { // run forever
                // 4. select ready SelectionKey for I/O operation
                if (selector.select(3000) == 0) {
                    continue;
                }
                // 5. get selected keys
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                // 6. handle selected key's interest operations
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    SocketChannel clientChannel = null;
                    try {
                        if (key.isAcceptable()) {
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                            // get socket channel from server socket channel
                            clientChannel = serverSocketChannel.accept();
                            // must to be nonblocking before register with selector
                            clientChannel.configureBlocking(false);
                            // register socket channel to selector with OP_READ
                            clientChannel.register(key.selector(), SelectionKey.OP_READ);
                            //用UUID，标识客户端client
                            // String clientKey = UUID.fastUUID().toString();
                            // Client client = new Client();
                            // client.setClientId(clientKey);
                            // client.setSocketChannel(clientChannel);
                            // msInsService.getClients().put(clientKey, client);
                        }

                        if (key.isReadable()) {
                            // read bytes from socket channel to byte buffer
                            clientChannel = (SocketChannel) key.channel();
                            ByteBuffer readBuffer = ByteBuffer.allocate(SOCKET_MESSAGE_SIZE);
                            int readBytes = clientChannel.read(readBuffer);

                            if (readBytes == -1) {

                                clientChannel.close();
                                // 将该client从clientMap中移除
                                msInsService.removeSocketChannel(clientChannel);
                                log.info("socket closed....... current connecting client number: " + msInsService.getMsrInsColl().size());

                            } else if (readBytes > 0) {
                                String receiveMassage = new String(readBuffer.array());
                                receiveMassage = receiveMassage.trim();
                                log.info("recv: " + receiveMassage);

                                // 与客户端的交互写在这里
                                msInsService.stateSelector(clientChannel, receiveMassage);

                                // Client client = msInsService.getConnectingChannel(clientChannel);
                                // if (client != null){
                                //     client.setRecvMessage(receiveMassage);
                                //
                                //     // attachment is content used to write
                                //     // TODO: 2022/10/20 与客户端的交互写在这里
                                //     // key.interestOps(SelectionKey.OP_WRITE);
                                //     // key.attach("send - " + receiveMassage);
                                //     msInsService.stateSelector(clientChannel, receiveMassage);
                                // } else {
                                //     throw new ServiceException("未能找到客户端");
                                // }


                                log.info("current connecting client number :" + msInsService.getMsrInsColl().size());
                            }
                        }

                        if (key.isValid() && key.isWritable()) {
                            // sendMessage2Client(key);

                            // === example ===
                            // clientChannel = (SocketChannel) key.channel();
                            // // get content from attachment
                            // String content = (String) key.attachment();
                            // // write content to socket channel
                            // clientChannel.write(ByteBuffer.wrap(content.getBytes()));
                            // key.interestOps(SelectionKey.OP_READ);
                        }

                    }catch (Exception e){
                        log.error(e.getMessage());
                        // 关闭client连接
                        if (clientChannel != null){

                            try {
                                //出错的信息发送给客户端
                                // msInsService.stateSelector(clientChannel);
                                msInsService.kill(clientChannel, e.getMessage());

                            } catch (ServiceException ex){

                            } finally {
                                clientChannel.close();
                                msInsService.removeSocketChannel(clientChannel);
                            }

                        }
                        log.info("current connecting client number: " + msInsService.getMsrInsColl().size());
                    }finally {
                        // remove handled key from selected keys
                        iterator.remove();
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // close selector
            if (selector != null) {
                try {
                    selector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 把消息发送给客户端
     * @param key
     * @return void
     * @author 7bin
     **/
    private void sendMessage2Client(SelectionKey key) throws IOException {
        //将消息发回给该client
        SocketChannel clientChannel = (SocketChannel) key.channel();
        // get content from attachment
        String content = (String) key.attachment();
        // write content to socket channel
        clientChannel.write(ByteBuffer.wrap(content.getBytes()));
        // ByteBuffer writeBuffer = ByteBuffer.allocate(SOCKET_MESSAGE_SIZE);
        // writeBuffer.put((content).getBytes());
        // writeBuffer.flip();
        // clientChannel.write(writeBuffer);
        key.interestOps(SelectionKey.OP_READ);
    }

    // /**
    //  * 把消息发送给客户端
    //  * @param clientChannel
    //  * @return void
    //  * @author 7bin
    //  **/
    // private void sendMessage2Client(SocketChannel clientChannel) throws IOException {
    //     //将消息发回给该client
    //     Client connectingChannel = msInsService.getConnectingChannel(clientChannel);
    //     clientChannel.write(ByteBuffer.wrap(connectingChannel.getSendMessage().getBytes()));
    // }




}
