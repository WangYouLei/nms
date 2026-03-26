package com.wang.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 评论Mapper接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 动态更新评论信息，只更新非 null 的字段
     * @param comment 评论信息
     * @return 影响行数
     */
    int update(Comment comment);

    /**
     * 增加回复数
     * @param commentId 评论ID
     * @return 影响行数
     */
    @Update("UPDATE comment SET reply_count = reply_count + 1, update_time = NOW() WHERE id = #{commentId}")
    int incrementReplyCount(@Param("commentId") Long commentId);

    /**
     * 减少回复数
     * @param commentId 评论ID
     * @return 影响行数
     */
    @Update("UPDATE comment SET reply_count = GREATEST(reply_count - 1, 0), update_time = NOW() WHERE id = #{commentId}")
    int decrementReplyCount(@Param("commentId") Long commentId);
}