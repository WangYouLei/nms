package com.wang.novel.listener;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wang.common.event.AuthorUpdatedEvent;
import com.wang.novel.mapper.NovelMapper;
import com.wang.pojo.entity.Novel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 作者信息变更事件消费者（novel-server）
 * 监听 RabbitMQ 队列 "event.author.updated.novel"，同步更新 novel 表中的冗余字段
 * （authorName、authorAvatar、authorRank）
 *
 * 使用 @RabbitListener 自动确认模式（默认），消息消费成功后自动 ACK
 * 如果消费失败，消息会根据 RabbitMQ 配置进行重试或进入死信队列
 */
@Slf4j
@Component
public class AuthorUpdatedListener {

    private final NovelMapper novelMapper;

    public AuthorUpdatedListener(NovelMapper novelMapper) {
        this.novelMapper = novelMapper;
    }

    /**
     * 消费作者信息变更事件，批量更新小说表冗余字段
     * @param event 作者信息变更事件
     */
    @RabbitListener(queues = AuthorUpdatedEvent.NOVEL_QUEUE)
    public void onAuthorUpdated(AuthorUpdatedEvent event) {
        try {
            log.info("收到作者信息变更事件：authorId={}, name={}", event.getAuthorId(), event.getName());

            // 使用 LambdaUpdateWrapper 批量更新，一条 SQL 搞定
            LambdaUpdateWrapper<Novel> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Novel::getAuthorId, event.getAuthorId())
                    .eq(Novel::getIsDel, false)
                    .set(Novel::getAuthorName, event.getName())
                    .set(Novel::getAuthorAvatar, event.getAvatar())
                    .set(Novel::getAuthorRank, event.getRank())
                    .set(Novel::getUpdateTime, LocalDateTime.now());

            int updateCount = novelMapper.update(null, updateWrapper);
            log.info("同步更新小说冗余字段完成：authorId={}, 更新数量={}", event.getAuthorId(), updateCount);
        } catch (Exception e) {
            log.error("处理作者信息变更事件异常：authorId={}, error={}", event.getAuthorId(), e.getMessage());
            throw e; // 重新抛出，让 RabbitMQ 进行重试
        }
    }
}
