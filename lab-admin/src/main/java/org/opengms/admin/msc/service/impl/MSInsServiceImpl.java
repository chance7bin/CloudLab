package org.opengms.admin.msc.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.opengms.admin.exception.ServiceException;
import org.opengms.admin.msc.entity.bo.Log;
import org.opengms.admin.msc.entity.bo.MsrIns;
import org.opengms.admin.msc.entity.bo.SubIdentifier;
import org.opengms.admin.msc.entity.bo.mdl.ModelClass;
import org.opengms.admin.msc.entity.po.ModelService;
import org.opengms.admin.msc.entity.socket.Client;
import org.opengms.admin.msc.enums.DataMIME;
import org.opengms.admin.msc.enums.ProcessState;
import org.opengms.admin.msc.enums.ProcessStatus;
import org.opengms.admin.msc.service.IMSInsService;
import org.opengms.admin.msc.service.IMdlService;
import org.opengms.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 7bin
 * @date 2022/10/20
 */
@Slf4j
@Service
public class MSInsServiceImpl implements IMSInsService {

    @Autowired
    IMdlService mdlService;

    // 当前socket通信实例
    private SocketChannel channel;

    //记录客户端信息，方便内容分发，一个客户端就对应一个模型运行实例
    // private final Map<String, Client> clients = new HashMap<>();

    //模型运行实例集合
    //一个模型运行实例对应一个客户端
    //记录客户端信息，方便内容分发
    //key为MsrIns的msriId
    private final Map<String, MsrIns> msrInsColl = new HashMap<>();

    // 模型运行实例
    private volatile MsrIns msrInsSingleton = null;

    // 模型容器根目录
    private final String ROOT_PATH = "E:/opengms-lab/msc";

    // 模型容器映射库目录 一些系统依赖的文件 msyh.ttf
    private final String MAPPING_LIB_DIR = ROOT_PATH + "/mapping_lib_dir";

    // 模型服务目录
    private final String MODEL_SERVICE_PATH = ROOT_PATH + "/model_service";

    // 临时的模型名称
    private final String MODEL_NAME = "createWorld";

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }


    @Override
    public Map<String, Client> getClients() {
        return null;
    }

    @Override
    public Map<String, MsrIns> getMsrInsColl() {
        return msrInsColl;
    }

    @Override
    public void stateSelector(SocketChannel channel) {
        stateSelector(channel, null);
    }

    //单例创建模型运行实例对象
    private void createMsrIns(){
        if(msrInsSingleton == null){
            synchronized (MsrIns.class) {
                if(msrInsSingleton == null){
                    log.info("createMsrIns");
                    msrInsSingleton = new MsrIns();
                }
            }
        }
    }

    // 绑定socket channel到模型运行实例中
    private MsrIns bindChannel2MsrIns(String msrInsId, SocketChannel channel){
        MsrIns msrIns = msrInsColl.get(msrInsId);
        if (msrIns.getSocketChannel() == null){
            msrIns.setSocketChannel(channel);
        }
        return msrIns;
    }


    @Override
    public void stateSelector(SocketChannel channel, String recvMessage) {
        // String recvMessage = client.getRecvMessage();
        // 设置通信实例
        setChannel(channel);

        // 创建模型运行实例
        // createMsrIns();

        // 如果接受的字符串是空的话直接结束后续操作
        if (StringUtils.isEmpty(recvMessage)){
            // currentMsrIns = getCurrentMsrIns(channel);
            return;
        }


        // 获取当前操作步骤
        int opLeft = recvMessage.indexOf('{');
        int opRight = recvMessage.indexOf('}');
        if(opLeft == -1 || opRight == -1 || opRight < opLeft){
            kill("illegal command! Can not find OP. Original cmd : " + recvMessage, ProcessState.INIT);
            throw new ServiceException("illegal command! Can not find OP. Original cmd : " + recvMessage);
        }
        String opStr = recvMessage.substring(opLeft + 1, opRight);
        ProcessState op = ProcessState.getStateByValue(opStr);

        // 获取实例id
        int idRight = recvMessage.indexOf('&');
        String insId = "";
        if(idRight == -1){
            insId = recvMessage.substring(opRight + 1);
            // id = id.replace('\0','');
        }
        else if(opRight > idRight){
            kill("illegal command! Can not find ID. Original cmd : " + recvMessage, ProcessState.INIT);
            throw new ServiceException("illegal command! Can not find ID. Original cmd : " + recvMessage);
        }
        else{
            insId = recvMessage.substring(opRight + 1, idRight);
        }

        // msrInsSingleton.setMsriId(insId);
        // 绑定socket channel到模型运行实例中
        MsrIns msrIns = bindChannel2MsrIns(insId, channel);

        opLeft = recvMessage.indexOf('}');
        // 剩余字符串
        String cmd = recvMessage.substring(opLeft + 1);
        // cmd = cmd.replace('\0','');
        if (op == null){
            kill("未找到相应步骤状态", msrIns.getCurrentState());
            throw new ServiceException("未找到相应步骤状态");
        }

        msrIns.setCurrentState(op);

        switch(op){
            case INIT:{
                initialize(insId);
                break;
            }
            case ON_ENTER_STATE:{
                String[] queryStr = cmd.split("&");
                String state = queryStr[1];

                enterState(insId, state);
                break;
            }
            case ON_FIRE_EVENT:{
                String[] queryStr = cmd.split("&");
                String state = queryStr[1];
                String event = queryStr[2];
                fireEvent(insId, state, event);
                break;
            }
            case ON_REQUEST_DATA:{
                //! querys
                String[] queryStr = cmd.split("&");
                String state = queryStr[1];
                String event = queryStr[2];

                String dataMIME = cmd.substring(cmd.indexOf("[") + 1, cmd.indexOf("]"));
                DataMIME mime = DataMIME.valueOf(dataMIME);
                // String data = cmd.substring(cmd.lastIndexOf("]") + 1);
                requestData(insId, state, event, mime);
                break;
            }
            case ON_RESPONSE_DATA:{
                String[] queryStr = cmd.split("&");

                //! querys
                String state = queryStr[1];
                String event = queryStr[2];

                //! data
                String data = cmd.substring(cmd.lastIndexOf("]") + 1);
                int left = cmd.indexOf("[");
                int right = cmd.indexOf("]");
                String dataSignal = cmd.substring(left + 1, right);
                String dataRemained = cmd.substring(right + 1);
                String dataMIME = dataRemained.substring(dataRemained.indexOf("[") + 1, dataRemained.indexOf("]"));
                DataMIME mime = DataMIME.valueOf(dataMIME);

                responseData(insId,state,event,dataSignal,mime,data);

                break;
            }
            case ON_POST_ERROR_INFO:{
                String errorinfo = "";
                String[] queryStr = cmd.split("&");
                errorinfo = queryStr[1];

                postErrorInfo(insId, errorinfo);
                break;
            }
            case ON_POST_WARNING_INFO:{
                String warninginfo = "";
                String[] queryStr = cmd.split("&");
                warninginfo = queryStr[1];

                postWarningInfo(insId, warninginfo);
                break;
            }
            case ON_POST_MESSAGE_INFO:{
                String messageinfo = "";
                String[] queryStr = cmd.split("&");
                messageinfo = queryStr[1];

                postMessageInfo(insId, messageinfo);
                break;
            }
            case ON_LEAVE_STATE:{
                String[] queryStr = cmd.split("&");
                String state = queryStr[1];
                leaveState(insId, state);
                break;
            }
            case ON_FINALIZE:{
                eFinalize(insId);
                break;
            }
            default:{
                kill("未知错误", msrIns.getCurrentState());
                break;
            }
        }

    }


    /**
     * 根据clientChannel得到正在连接的实例Client
     * @param clientChannel
     * @return Client
     * @author 7bin
     **/
    // @Override
    // public Client getConnectingChannel(SocketChannel clientChannel){
    //     Client connectingClient = null;
    //     for (Map.Entry<String, Client> entry : clients.entrySet()){
    //         Client value = entry.getValue();
    //         if (clientChannel == value.getSocketChannel()){
    //             connectingClient = value;
    //             break;
    //         }
    //     }
    //     return connectingClient;
    // }

    /**
     * 根据clientChannel得到正在连接的实例Client
     * @param clientChannel
     * @return Client
     * @author 7bin
     **/
    @Override
    public MsrIns getCurrentMsrIns(SocketChannel clientChannel){
        MsrIns currentMsrIns = null;
        for (Map.Entry<String, MsrIns> entry : msrInsColl.entrySet()){
            MsrIns value = entry.getValue();
            if (clientChannel == value.getSocketChannel()){
                currentMsrIns = value;
                break;
            }
        }
        return currentMsrIns;
    }

    @Override
    public MsrIns getCurrentMsrIns(String msInsId) {
        return msrInsColl.get(msInsId);
    }

    /**
     *
     * @param clientChannel
     * @return void
     * @author 7bin
     **/
    @Override
    public void removeSocketChannel(SocketChannel clientChannel){
        MsrIns currentMsrIns = getCurrentMsrIns(clientChannel);
        if (currentMsrIns != null){
            msrInsColl.remove(currentMsrIns.getMsriId());
        }
    }

    /**
     * 抽取标识符 根据{}分隔
     * @param operMsg 用来操作分隔的字符串
     * @param recvMsg 原始接收到的字符串
     * @param state 执行到的步骤
     * @return java.lang.String
     * @author 7bin
     **/
    private SubIdentifier extractIdentifier(String operMsg, String recvMsg, ProcessState state){
        int left = operMsg.indexOf('{');
        int right = operMsg.indexOf('}');
        if(left == -1 || right == -1 || right < left){
            // log.error("illegal command in " + state.toString() + ". Original cmd : " + msg);
            throw new ServiceException("illegal command in " + state.toString() + ". Original cmd : " + recvMsg);
        }
        String identifier = operMsg.substring(right + 1, right - left - 1);

        return new SubIdentifier(left, right, identifier);
    }

    // @Override
    private void initialize(String insId) {
        String msg = "{Initialized}" + insId + "[" + MAPPING_LIB_DIR + "]" + "[" + MODEL_SERVICE_PATH + "/" + MODEL_NAME + "/instance/" + insId + "]";
        sendMessage2Client(msg);
    }

    // @Override
    private void enterState(String insId, String state) {
        String msg = "{Enter State Notified}";
        sendMessage2Client(msg);
    }

    // @Override
    private void fireEvent(String insId, String state, String event) {
        String msg = "{Fire Event Notified}";
        sendMessage2Client(msg);
    }

    // @Override
    private void requestData(String insId, String state, String event, DataMIME dataMIME) {

        String templateInputData = "";

        MsrIns msrIns = getCurrentMsrIns(insId);
        ModelService modelService = msrIns.getModelService();
        // ModelClass modelClass = msrIns.getModelClass();
        String parameter = mdlService.getParameter(modelService, state, event);

        // if (event.equals("语言")){
        //     templateInputData = "English";
        // } else if (event.equals("文本")){
        //     templateInputData = "E:\\opengms-lab\\container\\workspace\\jupyter_cus_5.0_8268889755334766592\\hamlet.txt";
        // }

        sendMessage2Client("{Request Data Notified}[" + ProcessStatus.FINISH + "][" + dataMIME + "]" + parameter);

    }

    // @Override
    private void responseData(String insId, String state, String event, String dataSignal, DataMIME dataMIME, String data) {
        sendMessage2Client("{Response Data Received}" + insId);
    }

    // @Override
    private void postErrorInfo(String insId, String errorInfo) {
        sendMessage2Client("{Post Error Info Notified}" + insId);
    }

    // @Override
    private void postWarningInfo(String insId, String warningInfo) {
        sendMessage2Client("{Post Warning Info Notified}" + insId);
    }

    // @Override
    private void postMessageInfo(String insId, String messageInfo) {
        sendMessage2Client("{Post Message Info Notified}" + insId);
    }

    // @Override
    private void leaveState(String insId, String state) {
        sendMessage2Client("{Leave State Notified}");
    }

    // @Override
    private void eFinalize(String insId) {
        sendMessage2Client("{Finalize Notified}");
    }

    @Override
    public void kill(String msg){
        kill(channel, msg);
    }

    @Override
    public void kill(SocketChannel channel, String msg) {
        sendMessage2Client(channel, msg);
    }

    @Override
    public void kill(String msg, ProcessState state){
        sendMessage2Client("{kill}[" + state + "]" + msg);
    }

    /**
     * 把消息发送给客户端
     * @param message
     * @return void
     * @author 7bin
     **/
    private void sendMessage2Client(String message) {
        sendMessage2Client(channel, message);
    }

    private void sendMessage2Client(SocketChannel channel, String message) {
        log.info("send: " + message);
        try {
            // message = message + "(" + msrIns.getMsri_id() + ")";
            // 将消息发回给该client
            channel.write(ByteBuffer.wrap(message.getBytes()));
        } catch (IOException e){
            throw new ServiceException("socket 发送给 client 出错: " + e.getMessage());
        }
    }
}
