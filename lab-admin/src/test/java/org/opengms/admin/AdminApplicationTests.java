package org.opengms.admin;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.http.HttpUtil;
import com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.handler.codec.http.HttpConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.opengms.admin.clients.DriveClient;
import org.opengms.admin.entity.bo.Server;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.common.utils.uuid.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.HashMap;

/**
 * @author 7bin
 * @date 2022/11/17
 */
@SpringBootTest
@Slf4j
public class AdminApplicationTests {

    // 得到服务器信息
    @Test
    void getOSInfo() throws Exception {
        Server server = new Server();
        server.copyTo();
        System.out.println(server);

    }

    @Autowired
    DriveClient driveClient;

    @Value(value = "${labDriveUrl}")
    String labDriveUrl;

    @Test
    void download(){
        String url = labDriveUrl + "/file/download/" + 8286661556551323648L;

        ApiResponse fileInfo = driveClient.getFileInfo(8286661556551323648L);
        boolean b = ApiResponse.reqSuccess(fileInfo);
        HashMap<String, Object> responseData = ApiResponse.getResponseData(fileInfo);
        String suffix = (String)responseData.get("suffix");
        String filename = "gd_" + UUID.fastUUID() + "." + suffix;
        long fileSize = HttpUtil.downloadFile(url, new File("E:\\opengms-lab\\container\\pod\\8287025736412860416\\service\\1test1_8287938606320357376\\" + filename));
        System.out.println(fileSize);


    }

}
