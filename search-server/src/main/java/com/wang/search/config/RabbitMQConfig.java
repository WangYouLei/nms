package com.wang.search.config;

import com.wang.common.event.AuthorUpdatedEvent;
import com.wang.common.event.NovelUpdatedEvent;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * search-server 的 RabbitMQ 配置
 * 声明搜索服务消费所需的 Queue 和 Binding
 */
@Configuration
public class RabbitMQConfig {

    // ==================== 小说变更事件 ====================

    @Bean
    public DirectExchange novelEventExchange() {
        return new DirectExchange(NovelUpdatedEvent.EXCHANGE, true, false);
    }

    @Bean
    public Queue novelUpdatedSearchQueue() {
        return new Queue(NovelUpdatedEvent.SEARCH_QUEUE, true);
    }

    @Bean
    public Binding novelUpdatedSearchBinding() {
        return BindingBuilder.bind(novelUpdatedSearchQueue())
                .to(novelEventExchange())
                .with(NovelUpdatedEvent.SEARCH_ROUTING_KEY);
    }

    // ==================== 作者变更事件 ====================

    @Bean
    public DirectExchange authorEventExchange() {
        return new DirectExchange(AuthorUpdatedEvent.EXCHANGE, true, false);
    }

    @Bean
    public Queue authorUpdatedSearchQueue() {
        return new Queue(AuthorUpdatedEvent.SEARCH_QUEUE, true);
    }

    @Bean
    public Binding authorUpdatedSearchBinding() {
        return BindingBuilder.bind(authorUpdatedSearchQueue())
                .to(authorEventExchange())
                .with(AuthorUpdatedEvent.SEARCH_ROUTING_KEY);
    }

    // ==================== 消息转换器 ====================

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
