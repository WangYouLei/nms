package com.wang.comment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.result.Result;
import com.wang.pojo.dto.CommentDTO;
import com.wang.pojo.dto.CommentQueryDTO;
import com.wang.pojo.vo.CommentVO;

import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService {

    /**
     * 发表评论
     * @param commentDTO 评论信息
     * @return 发表结果
     */
    Result addComment(CommentDTO commentDTO);

    /**
     * 删除评论
     * @param commentId 评论ID
     * @param userId 用户ID（用于权限校验）
     * @return 删除结果
     */
    Result deleteComment(Long commentId, Long userId);

    /**
     * 更新评论
     * @param commentDTO 评论信息
     * @return 更新结果
     */
    Result updateComment(CommentDTO commentDTO);

    /**
     * 获取评论详情
     * @param commentId 评论ID
     * @return 评论详情
     */
    Result getCommentById(Long commentId);

    /**
     * 分页查询评论列表
     * @param queryDTO 查询条件
     * @return 评论列表
     */
    Result getCommentList(CommentQueryDTO queryDTO);

    /**
     * 获取小说的一级评论列表（带回复）
     * @param novelId 小说ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 评论列表
     */
    Result getNovelComments(Long novelId, Integer pageNum, Integer pageSize);

    /**
     * 获取评论的回复列表
     * @param rootId 根评论ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 回复列表
     */
    Result getCommentReplies(Long rootId, Integer pageNum, Integer pageSize);

    /**
     * 获取我的评论
     * @param userId 用户ID
     * @param userType 用户类型：1-访客，2-作者，3-管理员
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 我的评论列表
     */
    Result getMyComments(Long userId, Integer userType, Integer pageNum, Integer pageSize);

    /**
     * 审核评论
     * @param commentId 评论ID
     * @param auditLevel 审核层级
     * @return 审核结果
     */
    Result auditComment(Long commentId, Integer auditLevel);

    /**
     * 获取小说的评论树（包含所有回复）
     * @param novelId 小说ID
     * @param targetType 评论对象类型（1-小说，2-章节，null-全部）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 评论树列表
     */
    Result getNovelCommentTree(Long novelId, Integer targetType, Integer pageNum, Integer pageSize);

    // ==================== 管理端方法 ====================

    /**
     * 管理员删除任意评论
     * @param commentId 评论ID
     * @return 删除结果
     */
    Result managerDeleteComment(Long commentId);

    /**
     * 管理员批量删除评论
     * @param ids 评论ID列表
     * @return 删除结果
     */
    Result managerBatchDeleteComment(List<Long> ids);

    /**
     * 管理员批量审核评论
     * @param ids 评论ID列表
     * @param auditLevel 审核层级
     * @return 审核结果
     */
    Result managerBatchAuditComment(List<Long> ids, Integer auditLevel);
}