package com.wang.search.listener;

import com.wang.common.event.NovelUpdatedEvent;
import com.wang.search.document.NovelDocument;
import com.wang.search.repository.NovelSearchRepository;
import com.wang.search.service.DataSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 小说变更事件消费者（search-server）
 * 监听 RabbitMQ 队列 "event.novel.updated.search"，同步更新 ES 索引
 */
@Slf4j
@Component
public class NovelUpdatedListener {

    private final DataSyncService dataSyncService;
    private final NovelSearchRepository novelSearchRepository;

    public NovelUpdatedListener(DataSyncService dataSyncService,
                                NovelSearchRepository novelSearchRepository) {
        this.dataSyncService = dataSyncService;
        this.novelSearchRepository = novelSearchRepository;
    }

    @RabbitListener(queues = NovelUpdatedEvent.SEARCH_QUEUE)
    public void onNovelUpdated(NovelUpdatedEvent event) {
        try {
            log.info("收到小说变更事件：novelId={}, eventType={}", event.getNovelId(), event.getEventType());

            switch (event.getEventType()) {
                case "CREATE":
                case "UPDATE":
                    dataSyncService.syncNovel(event.getNovelId());
                    break;
                case "DELETE":
                    // 逻辑删除：直接在 ES 中更新 isDel=true
                    Optional<NovelDocument> optional = novelSearchRepository.findById(event.getNovelId());
                    if (optional.isPresent()) {
                        NovelDocument doc = optional.get();
                        doc.setIsDel(true);
                        novelSearchRepository.save(doc);
                        log.info("ES中标记小说为已删除：novelId={}", event.getNovelId());
                    } else {
                        log.warn("ES中未找到小说，跳过删除同步：novelId={}", event.getNovelId());
                    }
                    break;
                default:
                    log.warn("未知的事件类型：{}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("处理小说变更事件异常：novelId={}, error={}", event.getNovelId(), e.getMessage());
            throw e;
        }
    }
}
