package com.wang.manage.config;

import com.wang.common.event.AuthorUpdatedEvent;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * author-server 的 RabbitMQ 配置
 * 只声明 Exchange（发布方只需知道往哪个 Exchange 发消息）
 * Queue 和 Binding 由各消费方自行声明
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange authorEventExchange() {
        return new DirectExchange(AuthorUpdatedEvent.EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
