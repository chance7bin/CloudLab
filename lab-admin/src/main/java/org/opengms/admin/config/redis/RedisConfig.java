package org.opengms.admin.config.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * redis配置
 *
 * @author bin
 * @date 2022/07/19
 */
@Configuration
@EnableCaching
public class RedisConfig {


    // 解决使用@Cacheable，redis数据库value乱码
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory){
        // Duration.ofSeconds(120)设置缓存默认过期时间120秒
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(120));
        // 解决使用@Cacheable，redis数据库value乱码
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));
        RedisCacheManager cacheManager = RedisCacheManager.builder(factory).cacheDefaults(config).build();
        return cacheManager;
    }


    // fastjson序列化
    // @Bean
    // @SuppressWarnings(value = { "unchecked", "rawtypes" })
    // public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory)
    // {
    //     RedisTemplate<String, Object> template = new RedisTemplate<>();
    //     template.setConnectionFactory(connectionFactory);
    //
    //     FastJson2JsonRedisSerializer serializer = new FastJson2JsonRedisSerializer(Object.class);
    //
    //     // 使用StringRedisSerializer来序列化和反序列化redis的key值
    //     template.setKeySerializer(new StringRedisSerializer());
    //     template.setValueSerializer(serializer);
    //
    //     // Hash的key也采用StringRedisSerializer的序列化方式
    //     template.setHashKeySerializer(new StringRedisSerializer());
    //     template.setHashValueSerializer(serializer);
    //
    //     template.afterPropertiesSet();
    //     return template;
    // }


    // jackson序列化
    @Bean
    public RedisTemplate<String, Object> template(RedisConnectionFactory factory) {
        // 创建RedisTemplate<String, Object>对象
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 配置连接工厂
        template.setConnectionFactory(factory);
        // redis key 序列化方式使用stringSerial
        template.setKeySerializer(new StringRedisSerializer());
        // redis value 序列化方式自定义
        // template.setValueSerializer(new GenericFastJsonRedisSerializer());
        template.setValueSerializer(valueSerializer());
        // redis hash key 序列化方式使用stringSerial
        template.setHashKeySerializer(new StringRedisSerializer());
        // redis hash value 序列化方式自定义
        // template.setHashValueSerializer(new GenericFastJsonRedisSerializer());
        template.setHashValueSerializer(valueSerializer());
        return template;
    }


    private RedisSerializer<Object> valueSerializer() {
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
            new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 此项必须配置，否则如果序列化的对象里边还有对象，会报如下错误：
        //     java.lang.ClassCastException: java.util.LinkedHashMap cannot be cast to XXX
        objectMapper.activateDefaultTyping(
            objectMapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY);
        // 旧版写法：
        // objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        return jackson2JsonRedisSerializer;
    }

    @Bean
    public DefaultRedisScript<Long> limitScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(limitScriptText());
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    /**
     * 限流脚本
     */
    private String limitScriptText() {
        return "local key = KEYS[1]\n" +
            "local count = tonumber(ARGV[1])\n" +
            "local time = tonumber(ARGV[2])\n" +
            "local current = redis.call('get', key);\n" +
            "if current and tonumber(current) > count then\n" +
            "    return tonumber(current);\n" +
            "end\n" +
            "current = redis.call('incr', key)\n" +
            "if tonumber(current) == 1 then\n" +
            "    redis.call('expire', key, time)\n" +
            "end\n" +
            "return tonumber(current);";
    }

}
