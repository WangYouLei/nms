package com.wang.manage.event;

import com.wang.common.event.AuthorUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 作者信息变更事件发布器
 * 通过 RabbitMQ 发布事件，通知 novel-server 和 visitor-server 同步冗余字段
 */
@Slf4j
@Component
public class AuthorEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public AuthorEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发布作者信息变更事件
     * 同时发送到 novel-server、visitor-server 和 search-server 的队列
     * @param event 事件对象
     */
    public void publishAuthorUpdated(AuthorUpdatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    AuthorUpdatedEvent.EXCHANGE,
                    AuthorUpdatedEvent.NOVEL_ROUTING_KEY,
                    event
            );

            rabbitTemplate.convertAndSend(
                    AuthorUpdatedEvent.EXCHANGE,
                    AuthorUpdatedEvent.VISITOR_ROUTING_KEY,
                    event
            );

            rabbitTemplate.convertAndSend(
                    AuthorUpdatedEvent.EXCHANGE,
                    AuthorUpdatedEvent.SEARCH_ROUTING_KEY,
                    event
            );

            log.info("发布作者信息变更事件：authorId={}, name={}", event.getAuthorId(), event.getName());
        } catch (Exception e) {
            log.error("发布作者信息变更事件失败：authorId={}, error={}", event.getAuthorId(), e.getMessage());
        }
    }
}
