package org.opengms.admin.service.impl;

import com.alibaba.fastjson2.JSONObject;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.opengms.admin.config.socket.CustomTimerTask;
import org.opengms.admin.entity.bo.socket.Client;
import org.opengms.admin.entity.po.system.SysLogininfor;
import org.opengms.admin.entity.po.system.SysOperLog;
import org.opengms.admin.service.IAsyncService;
import org.opengms.admin.service.ISysLogininforService;
import org.opengms.admin.service.ISysOperLogService;
import org.opengms.common.constant.Constants;
import org.opengms.common.utils.LogUtils;
import org.opengms.common.utils.ServletUtils;
import org.opengms.common.utils.StringUtils;
import org.opengms.common.utils.ip.AddressUtils;
import org.opengms.common.utils.ip.IpUtils;
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
import java.util.*;

/**
 * @author bin
 * @date 2022/08/23
 */
@Slf4j
@Service
public class AsyncServiceImpl implements IAsyncService {

    @Autowired
    private ISysOperLogService sysOperLogService;

    @Autowired
    private ISysLogininforService sysLogininforService;

    @Value("${socket.port}")
    private int socketPort;

    // socket传输的字节大小
    private final static int SOCKET_MESSAGE_SIZE = 1024;

    /**
     * 操作日志记录
     *
     * @param operLog 操作日志信息
     * @return 任务task
     */
    @Async
    @Override
    public void recordOper(SysOperLog operLog) {

        // 远程查询操作地点
        operLog.setOperLocation(AddressUtils.getRealAddressByIP(operLog.getOperIp()));
        sysOperLogService.insertOperlog(operLog);

    }

    // 异步线程RequestContextHolder.getRequestAttributes()为null
    // @Async
    @Override
    public void recordLogininfor(String username, String status, String message, Object... args) {
        final UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        final String ip = IpUtils.getIpAddr(ServletUtils.getRequest());

        String address = AddressUtils.getRealAddressByIP(ip);
        StringBuilder s = new StringBuilder();
        s.append(LogUtils.getBlock(ip));
        s.append(address);
        s.append(LogUtils.getBlock(username));
        s.append(LogUtils.getBlock(status));
        s.append(LogUtils.getBlock(message));
        // 打印信息到日志
        log.info(s.toString(), args);
        // 获取客户端操作系统
        String os = userAgent.getOperatingSystem().getName();
        // 获取客户端浏览器
        String browser = userAgent.getBrowser().getName();
        // 封装对象
        SysLogininfor logininfor = new SysLogininfor();
        logininfor.setUserName(username);
        logininfor.setIpaddr(ip);
        logininfor.setLoginLocation(address);
        logininfor.setBrowser(browser);
        logininfor.setOs(os);
        logininfor.setMsg(message);
        // 日志状态
        if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER))
        {
            logininfor.setStatus(Constants.SUCCESS);
        }
        else if (Constants.LOGIN_FAIL.equals(status))
        {
            logininfor.setStatus(Constants.FAIL);
        }
        // 插入数据
        sysLogininforService.insertLogininfor(logininfor);


    }

    @Async
    @Override
    public void startSocketListener() {
        log.info("SocketListener - listening on port : " + socketPort);


    }

    //记录客户端信息，方便内容分发
    private static Map<String, Client> clients = new HashMap<>();

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
                            String clientKey = UUID.fastUUID().toString();
                            Client client = new Client();
                            client.setClientId(clientKey);
                            client.setSocketChannel(clientChannel);
                            clients.put(clientKey, client);
                        }

                        if (key.isReadable()) {
                            // read bytes from socket channel to byte buffer
                            clientChannel = (SocketChannel) key.channel();
                            ByteBuffer readBuffer = ByteBuffer.allocate(SOCKET_MESSAGE_SIZE);
                            int readBytes = clientChannel.read(readBuffer);

                            if (readBytes == -1) {

                                log.info("closed.......");
                                clientChannel.close();
                                // 将该client从clientMap中移除
                                removeSocketChannel(clientChannel);
                                log.info("current connecting client number: " + clients.size());

                            } else if (readBytes > 0) {
                                String receiveMassage = new String(readBuffer.array());
                                receiveMassage = receiveMassage.trim();
                                log.info("Client: " + receiveMassage);

                                Client client = getConnectingChannel(clientChannel);
                                if (client != null){
                                    client.setRecvMessage(receiveMassage);
                                }
                                // attachment is content used to write
                                // 要发送给client的消息写在这里
                                key.interestOps(SelectionKey.OP_WRITE);
                                key.attach("send - " + receiveMassage);

                                // Timer timer = new Timer();
                                // timer.schedule(new CustomTimerTask("PeriodDemo", key),1000L,1000L);


                                log.info("current connecting client number :" + clients.size());
                            }
                        }

                        if (key.isValid() && key.isWritable()) {
                            sendMessage2Client(key);

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
                            clientChannel.close();
                            removeSocketChannel(clientChannel);
                        }
                        log.info("current connecting client number: " + clients.size());
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

    /**
     * 根据clientChannel得到正在连接的实例Client
     * @param clientChannel
     * @return Client
     * @author 7bin
     **/
    private Client getConnectingChannel(SocketChannel clientChannel){
        Client connectingClient = null;
        for (Map.Entry<String, Client> entry : clients.entrySet()){
            Client value = entry.getValue();
            if (clientChannel == value.getSocketChannel()){
                connectingClient = value;
                break;
            }
        }
        return connectingClient;
    }

    /**
     *
     * @param clientChannel
     * @return void
     * @author 7bin
     **/
    private void removeSocketChannel(SocketChannel clientChannel){
        Client connectingChannel = getConnectingChannel(clientChannel);
        if (connectingChannel != null){
            clients.remove(connectingChannel.getClientId());
        }
    }

}
