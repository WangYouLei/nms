package com.wang.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 */
@Configuration
public class RedisConfig {

    /**
     * 配置RedisTemplate
     * 使用Jackson序列化，支持LocalDateTime等Java 8时间类型
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 配置ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        // 指定要序列化的域，field, get和set，以及修饰符范围
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 多态反序列化类型白名单：仅允许项目自身类与JDK基础类型，防止Redis数据被篡改后触发任意类反序列化
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.wang.")
                .allowIfSubType("java.util.")
                .allowIfSubType("java.lang.")
                .allowIfSubType("java.time.")
                .build();
        // 指定序列化输入的类型，类必须是非final修饰的
        objectMapper.activateDefaultTyping(typeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        // 支持Java 8时间类型
        objectMapper.registerModule(new JavaTimeModule());

        // 使用Jackson2JsonRedisSerializer序列化value
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = 
                new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        // 使用StringRedisSerializer序列化key
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }
}