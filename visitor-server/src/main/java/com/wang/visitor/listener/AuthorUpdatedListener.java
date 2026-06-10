package com.wang.visitor.listener;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wang.common.event.AuthorUpdatedEvent;
import com.wang.pojo.entity.VisitorFollow;
import com.wang.visitor.mapper.VisitorFollowMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 作者信息变更事件消费者（visitor-server）
 * 监听 RabbitMQ 队列 "event.author.updated.visitor"，同步更新 visitor_follow 表中的冗余字段
 * （authorName、authorAvatar、authorRank）
 *
 * 使用 @RabbitListener 自动确认模式（默认），消息消费成功后自动 ACK
 * 如果消费失败，消息会根据 RabbitMQ 配置进行重试或进入死信队列
 */
@Slf4j
@Component
public class AuthorUpdatedListener {

    private final VisitorFollowMapper visitorFollowMapper;

    public AuthorUpdatedListener(VisitorFollowMapper visitorFollowMapper) {
        this.visitorFollowMapper = visitorFollowMapper;
    }

    /**
     * 消费作者信息变更事件，批量更新关注表冗余字段
     * @param event 作者信息变更事件
     */
    @RabbitListener(queues = AuthorUpdatedEvent.VISITOR_QUEUE)
    public void onAuthorUpdated(AuthorUpdatedEvent event) {
        try {
            log.info("收到作者信息变更事件：authorId={}, name={}", event.getAuthorId(), event.getName());

            // 使用 LambdaUpdateWrapper 批量更新 visitor_follow 表
            LambdaUpdateWrapper<VisitorFollow> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(VisitorFollow::getAuthorId, event.getAuthorId())
                    .set(VisitorFollow::getAuthorName, event.getName())
                    .set(VisitorFollow::getAuthorAvatar, event.getAvatar())
                    .set(VisitorFollow::getAuthorRank, event.getRank())
                    .set(VisitorFollow::getUpdateTime, LocalDateTime.now());

            int count = visitorFollowMapper.update(null, updateWrapper);
            log.info("同步更新关注记录冗余字段完成：authorId={}, 更新数量={}", event.getAuthorId(), count);

            // visitor_collect 表没有 authorId 字段，无法直接按作者更新
            // 收藏记录中的作者信息来自小说的冗余字段，当 novel-server 同步了小说冗余字段后，
            // 新收藏的数据自然就是最新的，旧收藏数据的一致性可在用户查看时按需刷新
            log.info("收藏记录中的作者冗余字段将在 novel-server 同步小说冗余字段后，通过新收藏自然更新");
        } catch (Exception e) {
            log.error("处理作者信息变更事件异常：authorId={}, error={}", event.getAuthorId(), e.getMessage());
            throw e; // 重新抛出，让 RabbitMQ 进行重试
        }
    }
}
