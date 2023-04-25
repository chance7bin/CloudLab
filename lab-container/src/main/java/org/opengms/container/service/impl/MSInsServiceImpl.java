package org.opengms.container.service.impl;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.opengms.common.utils.DateUtils;
import org.opengms.common.utils.file.FileUtils;
import org.opengms.container.clients.DriveClient;
import org.opengms.container.constant.ContainerConstants;
import org.opengms.container.constant.ServiceType;
import org.opengms.container.constant.TaskStatus;
import org.opengms.container.entity.bo.InOutParam;
import org.opengms.container.entity.bo.Log;
import org.opengms.container.entity.dto.ApiResponse;
import org.opengms.container.entity.po.MsrIns;
import org.opengms.container.entity.bo.SubIdentifier;
import org.opengms.container.entity.po.ModelService;
import org.opengms.container.entity.po.docker.ContainerInfo;
import org.opengms.container.entity.socket.Client;
import org.opengms.container.enums.ContainerType;
import org.opengms.container.enums.DataMIME;
import org.opengms.container.enums.ProcessState;
import org.opengms.container.enums.DataFlag;
import org.opengms.container.exception.ServiceException;
import org.opengms.container.mapper.MsrInsMapper;
import org.opengms.container.service.*;
import org.opengms.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 7bin
 * @date 2022/10/20
 */
@Slf4j
@Service
public class MSInsServiceImpl implements IMSInsService {

    @Autowired
    MsrInsMapper msrInsMapper;

    @Override
    public MsrIns getMsrInsByMsriId(String msrInsId) {
        return msrInsMapper.selectByMsriId(msrInsId);
    }
}
