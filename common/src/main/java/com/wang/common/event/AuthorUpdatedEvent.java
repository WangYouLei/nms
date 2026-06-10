package com.wang.common.event;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 作者信息变更事件
 * 当作者修改昵称、头像、等级等信息时发布此事件
 * 订阅方（novel-server、visitor-server）接收后同步更新冗余字段
 *
 * Exchange 和 Queue 的命名常量供各服务自行声明 RabbitMQ 基础设施时使用
 */
@Data
public class AuthorUpdatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Exchange 名称 */
    public static final String EXCHANGE = "event.author.exchange";

    /** novel-server 的队列名称和 Routing Key */
    public static final String NOVEL_QUEUE = "event.author.updated.novel";
    public static final String NOVEL_ROUTING_KEY = "event.author.updated.novel";

    /** visitor-server 的队列名称和 Routing Key */
    public static final String VISITOR_QUEUE = "event.author.updated.visitor";
    public static final String VISITOR_ROUTING_KEY = "event.author.updated.visitor";

    /** search-server 的队列名称和 Routing Key */
    public static final String SEARCH_QUEUE = "event.author.updated.search";
    public static final String SEARCH_ROUTING_KEY = "event.author.updated.search";

    private Long authorId;
    private String name;
    private String avatar;
    private Integer rank;
    private String eventType; // CREATE / UPDATE / DELETE
    private LocalDateTime eventTime;

    public AuthorUpdatedEvent() {
        this.eventTime = LocalDateTime.now();
    }

    public AuthorUpdatedEvent(Long authorId, String name, String avatar, Integer rank) {
        this.authorId = authorId;
        this.name = name;
        this.avatar = avatar;
        this.rank = rank;
        this.eventType = "UPDATE";
        this.eventTime = LocalDateTime.now();
    }

    public AuthorUpdatedEvent(Long authorId, String eventType) {
        this.authorId = authorId;
        this.eventType = eventType;
        this.eventTime = LocalDateTime.now();
    }
}
