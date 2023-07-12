package org.opengms.admin.listener;

import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.common.utils.AddressUtils;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import lombok.extern.slf4j.Slf4j;
import org.opengms.admin.utils.redis.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * canal 监听mysql数据变化
 *
 * @author 7bin
 * @date 2023/07/12
 */
@Slf4j
// @Component
public class MysqlDataListener {


    private static final ThreadFactory springThreadFactory = new CustomizableThreadFactory("canal-pool-");
    private static final ExecutorService executors = Executors.newFixedThreadPool(1, springThreadFactory);

    @Autowired
    RedisCache redisCache;

    @PostConstruct
    private void startListening() {
        executors.submit(() -> {
            connector();
        });
    }

    void connector() {
        log.info("start listening mysql data change...");

        // 创建链接
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(AddressUtils.getHostIp(),
            11111), "example", "", "");
        int batchSize = 1000;
        int emptyCount = 0;
        try {
            connector.connect();
            connector.subscribe(".*\\..*");
            connector.rollback();
            int totalEmptyCount = 120;
            // while (emptyCount < totalEmptyCount) {
            while (true) {
                Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
                long batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId == -1 || size == 0) {
                    // emptyCount++;
                    // System.out.println("empty count : " + emptyCount);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    // emptyCount = 0;
                    // System.out.printf("message[batchId=%s,size=%s] \n", batchId, size);
                    printEntry(message.getEntries());
                }

                connector.ack(batchId); // 提交确认
                // connector.rollback(batchId); // 处理失败, 回滚数据
            }

            // System.out.println("empty too many times, exit");
        } finally {
            connector.disconnect();
        }
    }

    private void printEntry(List<Entry> entrys) {
        for (Entry entry : entrys) {
            if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
                continue;
            }

            RowChange rowChage = null;
            try {
                rowChage = RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
                    e);
            }

            EventType eventType = rowChage.getEventType();
            // System.out.println(String.format("================&gt; binlog[%s:%s] , name[%s,%s] , eventType : %s",
            //     entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
            //     entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
            //     eventType));

            for (RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == EventType.DELETE || eventType == EventType.UPDATE || eventType == EventType.INSERT) {
                    // 增删改操作删除redis缓存
                    // printColumn(rowData.getAfterColumnsList());
                    handleRedisCache(entry.getHeader().getTableName(), rowData.getAfterColumnsList());
                } else {
                    // 其他操作

                    // System.out.println("-------&gt; before");
                    // printColumn(rowData.getBeforeColumnsList());
                    // System.out.println("-------&gt; after");
                    // printColumn(rowData.getAfterColumnsList());
                }
            }
        }
    }

    private void printColumn(List<Column> columns) {
        for (Column column : columns) {
            log.info(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }

    private void handleRedisCache(String tableName, List<CanalEntry.Column> columns) {

        // 根据表名和字段名获取缓存key
        String key = getKey(tableName, columns);
        redisCache.del(key);

    }

    String getKey(String tableName, List<CanalEntry.Column> columns) {
        StringBuilder sb = new StringBuilder();
        sb.append(tableName);
        for (CanalEntry.Column column : columns) {
            if (column.getName().equals("user_id")) {
                sb.append(":").append(column.getValue());
            }
            // sb.append(":").append(column.getName()).append("=").append(column.getValue());
            // sb.append(":").append(column.getName()).append("=").append(column.getValue());
        }
        return sb.toString();
    }

}
