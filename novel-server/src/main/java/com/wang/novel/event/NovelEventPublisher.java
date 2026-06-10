package com.wang.novel.event;

import com.wang.common.event.NovelUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 小说信息变更事件发布器
 * 通过 RabbitMQ 发布事件，通知 search-server 同步 ES 索引
 */
@Slf4j
@Component
public class NovelEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public NovelEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发布小说变更事件
     * @param novelId 小说ID
     * @param eventType 事件类型：CREATE / UPDATE / DELETE
     */
    public void publishNovelUpdated(Long novelId, String eventType) {
        try {
            NovelUpdatedEvent event = new NovelUpdatedEvent(novelId, eventType);
            rabbitTemplate.convertAndSend(
                    NovelUpdatedEvent.EXCHANGE,
                    NovelUpdatedEvent.SEARCH_ROUTING_KEY,
                    event
            );
            log.info("发布小说变更事件：novelId={}, eventType={}", novelId, eventType);
        } catch (Exception e) {
            log.error("发布小说变更事件失败：novelId={}, eventType={}, error={}", novelId, eventType, e.getMessage());
        }
    }
}
