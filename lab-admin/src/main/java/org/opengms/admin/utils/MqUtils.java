package org.opengms.admin.utils;

import org.opengms.common.utils.spring.SpringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.nio.charset.StandardCharsets;

/**
 * 消息队列工具类
 *
 * @author 7bin
 * @date 2023/07/12
 */
public class MqUtils {

    /**
     * redis键
     *
     * @param redisKey redis键
     * @author 7bin
     **/
    public static void sendRedisKeyToMq(String redisKey) {
        // 1.准备消息
        Message message = MessageBuilder
            .withBody(redisKey.getBytes(StandardCharsets.UTF_8))
            .setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT)
            .build();
        // 2.发送消息
        RabbitTemplate rabbitTemplate = SpringUtils.getBean("rabbitTemplate");
        rabbitTemplate.convertAndSend("redis.queue", message);
    }

}
