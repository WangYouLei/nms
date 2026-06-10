package com.wang.visitor.config;

import com.wang.common.event.AuthorUpdatedEvent;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * visitor-server 的 RabbitMQ 配置
 * 只声明自己消费所需的 Queue 和 Binding
 * Exchange 由发布方（author-server）声明，此处也声明一份确保可用
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange authorEventExchange() {
        return new DirectExchange(AuthorUpdatedEvent.EXCHANGE, true, false);
    }

    @Bean
    public Queue visitorAuthorUpdatedQueue() {
        return new Queue(AuthorUpdatedEvent.VISITOR_QUEUE, true);
    }

    @Bean
    public Binding visitorAuthorUpdatedBinding() {
        return BindingBuilder.bind(visitorAuthorUpdatedQueue())
                .to(authorEventExchange())
                .with(AuthorUpdatedEvent.VISITOR_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
