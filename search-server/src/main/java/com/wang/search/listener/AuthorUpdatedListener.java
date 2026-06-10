package com.wang.search.listener;

import com.wang.common.event.AuthorUpdatedEvent;
import com.wang.search.document.AuthorDocument;
import com.wang.search.document.NovelDocument;
import com.wang.search.repository.AuthorSearchRepository;
import com.wang.search.repository.NovelSearchRepository;
import com.wang.search.service.DataSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 作者变更事件消费者（search-server）
 * 监听 RabbitMQ 队列 "event.author.updated.search"，同步更新 ES 索引
 * 同时级联更新该作者名下所有小说的冗余字段
 */
@Slf4j
@Component
public class AuthorUpdatedListener {

    private final DataSyncService dataSyncService;
    private final AuthorSearchRepository authorSearchRepository;
    private final NovelSearchRepository novelSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public AuthorUpdatedListener(DataSyncService dataSyncService,
                                 AuthorSearchRepository authorSearchRepository,
                                 NovelSearchRepository novelSearchRepository,
                                 ElasticsearchOperations elasticsearchOperations) {
        this.dataSyncService = dataSyncService;
        this.authorSearchRepository = authorSearchRepository;
        this.novelSearchRepository = novelSearchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @RabbitListener(queues = AuthorUpdatedEvent.SEARCH_QUEUE)
    public void onAuthorUpdated(AuthorUpdatedEvent event) {
        try {
            log.info("收到作者信息变更事件：authorId={}, name={}, eventType={}", event.getAuthorId(), event.getName(), event.getEventType());

            switch (event.getEventType() != null ? event.getEventType() : "UPDATE") {
                case "CREATE":
                case "UPDATE":
                    // 同步作者索引
                    dataSyncService.syncAuthor(event.getAuthorId());
                    // 级联更新小说索引中该作者的冗余字段
                    cascadeUpdateNovels(event);
                    break;
                case "DELETE":
                    // 逻辑删除：直接在 ES 中更新 isDel=true
                    Optional<AuthorDocument> optional = authorSearchRepository.findById(event.getAuthorId());
                    if (optional.isPresent()) {
                        AuthorDocument doc = optional.get();
                        doc.setIsDel(true);
                        authorSearchRepository.save(doc);
                        log.info("ES中标记作者为已删除：authorId={}", event.getAuthorId());
                    } else {
                        log.warn("ES中未找到作者，跳过删除同步：authorId={}", event.getAuthorId());
                    }
                    break;
                default:
                    log.warn("未知的事件类型：{}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("处理作者信息变更事件异常：authorId={}, error={}", event.getAuthorId(), e.getMessage());
            throw e;
        }
    }

    /**
     * 级联更新该作者名下所有未删除小说的 authorName/authorAvatar/authorRank
     */
    private void cascadeUpdateNovels(AuthorUpdatedEvent event) {
        try {
            Criteria criteria = new Criteria("authorId").is(event.getAuthorId())
                    .and(new Criteria("isDel").is(false));
            CriteriaQuery query = new CriteriaQuery(criteria);
            SearchHits<NovelDocument> hits = elasticsearchOperations.search(
                    query, NovelDocument.class, IndexCoordinates.of("novel_index"));

            List<NovelDocument> novels = hits.getSearchHits().stream()
                    .map(hit -> {
                        NovelDocument doc = hit.getContent();
                        if (event.getName() != null) doc.setAuthorName(event.getName());
                        if (event.getAvatar() != null) doc.setAuthorAvatar(event.getAvatar());
                        if (event.getRank() != null) doc.setAuthorRank(event.getRank());
                        return doc;
                    })
                    .toList();

            if (!novels.isEmpty()) {
                novelSearchRepository.saveAll(novels);
                log.info("级联更新小说索引中作者冗余字段：authorId={}, 更新{}本小说", event.getAuthorId(), novels.size());
            }
        } catch (Exception e) {
            log.error("级联更新小说索引失败：authorId={}, error={}", event.getAuthorId(), e.getMessage());
        }
    }
}
