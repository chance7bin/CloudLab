package org.opengms.admin.listener;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.opengms.admin.entity.bo.CanalMessage;
import org.opengms.admin.utils.redis.RedisCache;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author 7bin
 * @date 2023/07/12
 */
@Slf4j
// @Component
public class RabbitMqListener {

    @Autowired
    private RedisCache redisCache;

    @RabbitListener(queues = "redis.queue")
    public void listenRedisQueue(String msg) {
        log.info("清除缓存: [ key: {} ]", msg);
        redisCache.del(msg);
    }

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(name = "canal.queue"),
        exchange = @Exchange(name = "canal.fanout", type = ExchangeTypes.FANOUT),
        key = {"canal"}
    ))
    public void listenCanalQueue(Message mqMessage, Channel channel) {
        log.info("start listening mysql data change...");
        String message = new String(mqMessage.getBody(), StandardCharsets.UTF_8);
        // 解析message转换成CanalMessage对象
        CanalMessage canalMessage = JSONUtil.toBean(message, CanalMessage.class);

        String type = canalMessage.getType();
        if (type == null) {
            log.info("unknown type {}", canalMessage.getType());
            return;
        }

        if (type.equals("INSERT") || type.equals("UPDATE") || type.equals("DELETE")) {
            handleRedisCache(canalMessage.getTable(), canalMessage.getData());
        } else {
            log.info("ignore type {}", canalMessage.getType());
        }

    }

    private void handleRedisCache(String tableName, Object data) {

        // 根据表名和字段名获取缓存key
        String key = getKey(tableName, data);
        redisCache.del(key);
        log.info("清除缓存: [ key: {} ]", key);

    }

    String getKey(String tableName, Object data) {
        StringBuilder sb = new StringBuilder();
        sb.append(tableName);
        JSONObject jsonObject = JSONUtil.parseObj(data);
        sb.append(":").append(jsonObject.get("user_id"));
        return sb.toString();
    }

}
