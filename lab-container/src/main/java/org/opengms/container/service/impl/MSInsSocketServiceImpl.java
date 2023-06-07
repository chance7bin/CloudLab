package org.opengms.container.service.impl;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.opengms.common.utils.DateUtils;
import org.opengms.common.utils.StringUtils;
import org.opengms.common.utils.file.FileUtils;
import org.opengms.container.clients.DriveClient;
import org.opengms.container.constant.ContainerConstants;
import org.opengms.container.constant.ServiceMode;
import org.opengms.container.constant.ServiceType;
import org.opengms.container.constant.TaskStatus;
import org.opengms.container.entity.bo.InOutParam;
import org.opengms.container.entity.bo.Log;
import org.opengms.container.entity.bo.SubIdentifier;
import org.opengms.container.entity.dto.ApiResponse;
import org.opengms.container.entity.po.ModelService;
import org.opengms.container.entity.po.MsrIns;
import org.opengms.container.entity.po.docker.ContainerInfo;
import org.opengms.container.entity.socket.Client;
import org.opengms.container.enums.ContainerType;
import org.opengms.container.enums.DataFlag;
import org.opengms.container.enums.DataMIME;
import org.opengms.container.enums.ProcessState;
import org.opengms.container.exception.ServiceException;
import org.opengms.container.mapper.MsrInsMapper;
import org.opengms.container.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author 7bin
 * @date 2023/04/25
 */
@Slf4j
@Service
public class MSInsSocketServiceImpl implements IMSInsSocketService {

    @Autowired
    IMdlService mdlService;

    @Autowired
    DriveClient driveClient;

    @Value(value = "${container.repository}")
    private String repository;

    @Autowired
    MsrInsMapper msrInsMapper;

    @Autowired
    IDockerService dockerService;

    @Autowired
    IK8sService k8sService;

    @Autowired
    IContainerService containerService;

    @Value(value = "${modelService.mode}")
    String modelServiceMode;

    @Value(value = "${k8s.namespace}")
    String namespace;


    // 当前socket通信实例
    private Channel channel;

    //记录客户端信息，方便内容分发，一个客户端就对应一个模型运行实例
    // private final Map<String, Client> clients = new HashMap<>();

    //模型运行实例集合
    //一个模型运行实例对应一个客户端
    //记录客户端信息，方便内容分发
    //key为MsrIns的msriId
    private final Map<String, MsrIns> msrInsColl = new ConcurrentHashMap<>();
    //key: channel; value: instanceId
    private final Map<Channel, String> channelInsMap = new ConcurrentHashMap<>();

    // 模型容器根目录
    private final String ROOT_PATH = "E:/opengms-lab/msc";

    // 模型容器映射库目录 一些系统依赖的文件 msyh.ttf
    private final String MAPPING_LIB_DIR = ROOT_PATH + "/mapping_lib_dir";

    // 模型服务目录
    private final String MODEL_SERVICE_PATH = ROOT_PATH + "/model_service";

    // 临时的模型名称
    private final String MODEL_NAME = "createWorld";

    public void setChannel(Channel channel) {
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
    public Map<Channel, String> getChannelInsMap() {
        return channelInsMap;
    }

    @Override
    public void stateSelector(Channel channel) {
        stateSelector(channel, null);
    }

    // 绑定channel到模型运行实例中
    private MsrIns bindChannel2MsrIns(String msrInsId, Channel channel){
        MsrIns msrIns = msrInsColl.get(msrInsId);
        if (msrIns.getChannel() == null){
            msrIns.setChannel(channel);

            // channel 到 msrIns 的绑定
            channelInsMap.put(channel, msrInsId);
        }
        return msrIns;
    }

    @Async
    @Override
    public void stateSelector(Channel channel, String recvMessage) {

        // 设置通信实例
        setChannel(channel);

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


        MsrIns ins = getMsrInsFromMsrInsColl(insId);
        if (ins == null) {
            kill("未找到模型运行实例", ProcessState.INIT);
            throw new ServiceException("未找到模型运行实例");
        }

        // msrInsSingleton.setMsriId(insId);
        // 绑定socket channel到模型运行实例中
        MsrIns msrIns = bindChannel2MsrIns(insId, channel);

        // 记录日志信息到模型运行实例中
        // msrIns.getLogs().add(new Log(ProcessStatus.FINISH, recvMessage));

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
    @Override
    public MsrIns getCurrentMsrIns(Channel clientChannel){
        // MsrIns currentMsrIns = null;
        // for (Map.Entry<String, MsrIns> entry : msrInsColl.entrySet()){
        //     MsrIns value = entry.getValue();
        //     if (clientChannel == value.getChannel()){
        //         currentMsrIns = value;
        //         break;
        //     }
        // }
        String instanceId = channelInsMap.get(clientChannel);
        if (instanceId == null){
            return null;
        }

        return msrInsColl.get(instanceId);
    }

    @Override
    public MsrIns getCurrentMsrIns(String msInsId) {
        return msrInsColl.get(msInsId);
    }


    /**
     * 是否销毁模型关联的容器
     * @param modelService 模型服务实例
     * @return {@link Boolean}
     * @author 7bin
     **/
    Boolean isDestroyContainer(ModelService modelService){
        String serviceType = modelService.getServiceType();
        if (ServiceType.DISPOSABLE.equals(serviceType)){
            return true;
        }
        return false;
    }

    /**
     *
     * @param clientChannel
     * @return void
     * @author 7bin
     **/
    // @Override
    public void removeSocketChannel(Channel clientChannel){

        if(clientChannel != null){
            MsrIns currentMsrIns = getCurrentMsrIns(clientChannel);
            removeSocketChannel(currentMsrIns);
        }

    }


    public void removeSocketChannel(MsrIns msrIns){
        if (msrIns != null){

            // 两个map解绑
            msrInsColl.remove(msrIns.getMsriId());
            if (msrIns.getChannel() != null){
                channelInsMap.remove(msrIns.getChannel());
            }

            log.info("socket closed....... current connecting client number: " + msrInsColl.size());

            // 删除docker容器
            // 如果是只运行一次的服务的话，运行完删除运行容器
            if (isDestroyContainer(msrIns.getModelService())){
                ContainerInfo container = containerService.getContainerInfoById(msrIns.getContainerId(), ContainerType.JUPYTER);
                if (container != null){
                    if(ServiceMode.DOCKER.equals(modelServiceMode)){
                        dockerService.removeContainer(container.getContainerInsId());
                    } else {
                        k8sService.deletePod(container.getContainerName(), namespace);
                    }

                    containerService.deleteContainer(container.getContainerId(), ContainerType.JUPYTER);
                }
            }
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
        MsrIns ins = getMsrInsFromMsrInsColl(insId);
        ins.setStatus(TaskStatus.STARTED);
        ins.setStartTime(new Date());

        ins.getLogs().add(new Log(
            ProcessState.INIT,
            null, null,
            DataFlag.FINISH,
            null,
            new Date()
        ));
        msrInsMapper.updateById(ins);

        ModelService ms = ins.getModelService();
        // if (ms.getRelativeDir() == null){
        //     ms.setRelativeDir(ms.getMsName() + "_" + ms.getMsId());
        // }

        // 依赖文件
        String mappingLibDir;

        // （不需要了）新建环境和基于已有环境分开讨论
        // if (ms.getNewImage() != null && ms.getNewImage()){
        //     ins.setInstanceDir("/instance/" + insId);
        //     mappingLibDir = ContainerConstants.INNER_SERVICE_DIR + "/model";
        // } else {
        //     ins.setInstanceDir("/" + ms.getRelativeDir() + "/instance/" + insId);
        //     mappingLibDir = ContainerConstants.INNER_SERVICE_DIR + "/" + ms.getRelativeDir() + "/model";
        // }
        // ins.setInstanceDir("/" + ms.getMsId() + "/instance/" + insId);
        ins.setInstanceDir("/instance/" + insId);
        // TODO: 2022/12/2 MAPPING_LIB_DIR要放在哪里？
        // mappingLibDir = ContainerConstants.INNER_SERVICE_DIR + "/" + ms.getMsId() + "/model";
        mappingLibDir = ContainerConstants.INNER_SERVICE_DIR + "/model";

        String msg = "{Initialized}" + insId
            + "[" + mappingLibDir + "]"
            + "[" + ContainerConstants.INNER_SERVICE_DIR + ins.getInstanceDir() + "]";
        sendMessage2Client(msg);
    }

    // @Override
    private void enterState(String insId, String state) {
        MsrIns ins = getMsrInsFromMsrInsColl(insId);

        ins.getLogs().add(new Log(
            ProcessState.ON_ENTER_STATE,
            state, null,
            DataFlag.FINISH,
            null,
            new Date()
        ));
        msrInsMapper.updateById(ins);

        String msg = "{Enter State Notified}";
        sendMessage2Client(msg);
    }

    // @Override
    private void fireEvent(String insId, String state, String event) {
        MsrIns ins = getMsrInsFromMsrInsColl(insId);

        ins.getLogs().add(new Log(
            ProcessState.ON_FIRE_EVENT,
            state, event,
            DataFlag.FINISH,
            null,
            new Date()
        ));
        msrInsMapper.updateById(ins);

        String msg = "{Fire Event Notified}";
        sendMessage2Client(msg);
    }

    // @Override
    private void requestData(String insId, String state, String event, DataMIME dataMIME) {

        String templateInputData = "";

        MsrIns msrIns = getCurrentMsrIns(insId);
        ModelService modelService = msrIns.getModelService();
        // ModelClass modelClass = msrIns.getModelClass();

        String parameter = null;
        try {
            String serviceDir = ContainerConstants.SERVICE_DIR(msrIns.getModelService().getMsId());

            parameter = mdlService.getParameter(modelService, state, event, serviceDir, msrIns.getInstanceDir());

            msrIns.getLogs().add(new Log(
                ProcessState.ON_REQUEST_DATA,
                state, event,
                DataFlag.FINISH,
                "Get input parameter: [ " + parameter + " ]",
                new Date()
            ));

            // msrIns.getInputs().add(new InOutParam(state, event, dataMIME.getInfo(), parameter));

            msrInsMapper.updateById(msrIns);

            sendMessage2Client("{Request Data Notified}[" + DataFlag.FINISH + "][" + dataMIME + "]" + parameter);
        }catch (ServiceException se){
            kill(se.getMessage(), ProcessState.ON_REQUEST_DATA, state, event);
        }
        // if (event.equals("语言")){
        //     templateInputData = "English";
        // } else if (event.equals("文本")){
        //     templateInputData = "E:\\opengms-lab\\container\\workspace\\jupyter_cus_5.0_8268889755334766592\\hamlet.txt";
        // }


    }

    // @Override
    private void responseData(String insId, String state, String event, String dataSignal, DataMIME dataMIME, String data) {
        MsrIns ins = getMsrInsFromMsrInsColl(insId);
        ins.getLogs().add(new Log(
            ProcessState.ON_RESPONSE_DATA,
            state, event,
            DataFlag.FINISH,
            "Get output data: { " + dataSignal + " } [ " + data + " ]",
            new Date()
        ));

        try {
            // 该工作空间所在的容器创建的模型服务都在 container.repository 目录的 workspace/{containerId}/service 下
            // String SERVICE_DIR = "/workspace/" + ins.getModelService().getContainerId() + "/service";
            // String serviceDir = null;
            // if (ins.getModelService().getContainerId() != null){
            //     serviceDir = ContainerConstants.SERVICE_DIR(ins.getModelService().getContainerId());
            // } else {
            //     serviceDir = "/service/" + ins.getModelService().getMsName() + "_" + ins.getModelService().getPkgId();
            // }
            String serviceDir = ContainerConstants.SERVICE_DIR(ins.getModelService().getMsId());


            //将数据上传到drive中
            if (!data.contains(ContainerConstants.INNER_SERVICE_DIR)){
                throw new ServiceException("数据输出的文件夹路径有误，数据上传失败");
            }
            String[] split = data.split(ContainerConstants.INNER_SERVICE_DIR);
            // 宿主机中真实的文件路径
            String fileHostFile = repository + serviceDir + split[1];
            //上传
            ApiResponse response = driveClient.uploadFiles(FileUtils.file2MultipartFile(new File(fileHostFile)));
            if (!ApiResponse.isSuccess(response)){
                throw new ServiceException("数据上传至文件服务器时出错，数据上传失败");
            }
            String responseData = (String) ApiResponse.getResponseData(response);
            InOutParam inOutParam = new InOutParam(state, event, dataMIME.getInfo(), data);
            inOutParam.setDriveFileId(responseData);
            ins.getOutputs().add(inOutParam);

            msrInsMapper.updateById(ins);

            sendMessage2Client("{Response Data Received}" + insId);
        } catch (ServiceException se){
            kill(se.getMessage(), ProcessState.ON_RESPONSE_DATA, state, event);
        }

    }

    // @Override
    private void postErrorInfo(String insId, String errorInfo) {
        MsrIns ins = getMsrInsFromMsrInsColl(insId);
        ins.getLogs().add(new Log(
            ProcessState.ON_POST_ERROR_INFO,
            null, null,
            DataFlag.FINISH,
            errorInfo,
            new Date()
        ));
        msrInsMapper.updateById(ins);
        sendMessage2Client("{Post Error Info Notified}" + insId);
    }

    // @Override
    private void postWarningInfo(String insId, String warningInfo) {
        MsrIns ins = getMsrInsFromMsrInsColl(insId);
        ins.getLogs().add(new Log(
            ProcessState.ON_POST_WARNING_INFO,
            null, null,
            DataFlag.FINISH,
            warningInfo,
            new Date()
        ));
        msrInsMapper.updateById(ins);
        sendMessage2Client("{Post Warning Info Notified}" + insId);
    }

    // @Override
    private void postMessageInfo(String insId, String messageInfo) {
        MsrIns ins = getMsrInsFromMsrInsColl(insId);
        ins.getLogs().add(new Log(
            ProcessState.ON_POST_MESSAGE_INFO,
            null, null,
            DataFlag.FINISH,
            messageInfo,
            new Date()
        ));
        msrInsMapper.updateById(ins);
        sendMessage2Client("{Post Message Info Notified}" + insId);
    }

    // @Override
    private void leaveState(String insId, String state) {
        MsrIns ins = getMsrInsFromMsrInsColl(insId);
        ins.getLogs().add(new Log(
            ProcessState.ON_LEAVE_STATE,
            state, null,
            DataFlag.FINISH,
            null,
            new Date()
        ));
        msrInsMapper.updateById(ins);
        sendMessage2Client("{Leave State Notified}");
    }

    // @Override
    private void eFinalize(String insId) {
        MsrIns ins = getMsrInsFromMsrInsColl(insId);
        ins.getLogs().add(new Log(
            ProcessState.ON_FINALIZE,
            null, null,
            DataFlag.FINISH,
            null,
            new Date()
        ));
        ins.setStatus(TaskStatus.FINISHED);
        int second = DateUtils.differentSecondsByMillisecond(new Date(), ins.getStartTime());
        ins.setSpanTime(second);
        msrInsMapper.updateById(ins);
        sendMessage2Client("{Finalize Notified}");
    }




    /**
     * 移除socket实例以及该socket对应的模型运行实例
     * @param channel socket
     * @author 7bin
     **/
    @Async
    @Override
    public void removeChannelAndMsrInsColl(Channel channel){
        // 将该client从clientMap中移除
        removeSocketChannel(channel);
    }

    @Async
    @Override
    public void removeChannelAndMsrInsColl(MsrIns msrIns){
        // 将该client从clientMap中移除
        removeSocketChannel(msrIns);
    }

    // @Override
    // public void kill(String msg){
    //     kill(channel, msg);
    // }

    @Override
    public void kill(String msg, ProcessState processState, String state, String event){
        // kill("[" + state + "]" + msg);
        kill(channel, msg, processState, state, event);
    }

    @Override
    public void kill(String msg, ProcessState processState){
        // kill("[" + state + "]" + msg);
        kill(msg, processState, null, null);
    }

    @Override
    public void kill(Channel channel, String msg, ProcessState processState){
        // kill("[" + state + "]" + msg);
        kill(channel, msg, processState, null, null);
    }

    @Override
    public void kill(Channel channel, String msg, ProcessState processState, String state, String event) {
        MsrIns ins = getCurrentMsrIns(channel);
        ins.setStatus(TaskStatus.ERROR);

        ins.getLogs().add(new Log(
            processState,
            state, event,
            DataFlag.ERROR,
            msg,
            new Date()
        ));

        if (ins.getStartTime() == null){
            ins.setSpanTime(0);
        } else {
            int second = DateUtils.differentSecondsByMillisecond(new Date(), ins.getStartTime());
            ins.setSpanTime(second);
        }

        msrInsMapper.updateById(ins);

        sendMessage2Client(channel, "{kill}" + msg);


        // 当客户端断开时netty走 channelInactive 或者 exceptionCaught
        // 在上述两个地方移除map中对应的实例
        // removeChannelAndMsrInsColl(channel);

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

    private void sendMessage2Client(Channel channel, String message) {
        log.info("send: " + message);
        channel.writeAndFlush(message);
    }


    /**
     * 根据msrid从msrInsColl得到该模型运行实例的信息
     *
     * @param msriId
     * @return {@link MsrIns}
     * @author 7bin
     **/
    @Override
    public MsrIns getMsrInsFromMsrInsColl(String msriId){
        return msrInsColl.get(msriId);
    }
}
