package org.opengms.container.service;

import org.opengms.container.entity.po.InstanceDetail;
import org.opengms.container.entity.po.MsrIns;
import org.opengms.container.entity.po.MsrInsPlain;
import org.opengms.container.entity.socket.Client;
import org.opengms.container.enums.ProcessState;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

/**
 * 模型容器实例接口
 *
 * @author 7bin
 * @date 2022/10/19
 */
public interface IMSInsService {

    InstanceDetail getMsrInsByMsriId(String msInsId);

    List<MsrInsPlain> getMsrInsList();
}
