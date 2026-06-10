package com.wang.common.event;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小说信息变更事件
 * 当小说新增、修改、逻辑删除时发布此事件
 * 订阅方（search-server）接收后同步更新 ES 索引
 *
 * Exchange 和 Queue 的命名常量供各服务自行声明 RabbitMQ 基础设施时使用
 */
@Data
public class NovelUpdatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Exchange 名称 */
    public static final String EXCHANGE = "event.novel.exchange";

    /** search-server 的队列名称和 Routing Key */
    public static final String SEARCH_QUEUE = "event.novel.updated.search";
    public static final String SEARCH_ROUTING_KEY = "event.novel.updated.search";

    private Long novelId;
    private String eventType; // CREATE / UPDATE / DELETE
    private LocalDateTime eventTime;

    public NovelUpdatedEvent() {
        this.eventTime = LocalDateTime.now();
    }

    public NovelUpdatedEvent(Long novelId, String eventType) {
        this.novelId = novelId;
        this.eventType = eventType;
        this.eventTime = LocalDateTime.now();
    }
}
