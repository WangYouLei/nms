package com.wang.comment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.enums.AuditAimTypeEnum;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import org.springframework.beans.BeanUtils;
import com.wang.comment.mapper.CommentMapper;
import com.wang.comment.mapper.NovelMapper;
import com.wang.comment.mapper.UserAvatarMapper;
import com.wang.comment.service.CommentService;
import com.wang.common.utils.RoleContextUtil;
import com.wang.commonserver.service.AiAuditService;
import com.wang.commonserver.service.SensitiveWordService;
import com.wang.pojo.dto.CommentDTO;
import com.wang.pojo.dto.CommentQueryDTO;
import com.wang.pojo.entity.Comment;
import com.wang.pojo.vo.AuditResultVO;
import com.wang.pojo.vo.CommentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.List;

/**
 * 评论服务实现类
 */
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final AiAuditService aiAuditService;
    private final SensitiveWordService sensitiveWordService;
    private final NovelMapper novelMapper;
    private final UserAvatarMapper userAvatarMapper;

    public CommentServiceImpl(CommentMapper commentMapper, AiAuditService aiAuditService, 
                              SensitiveWordService sensitiveWordService, NovelMapper novelMapper,
                              UserAvatarMapper userAvatarMapper) {
        this.commentMapper = commentMapper;
        this.aiAuditService = aiAuditService;
        this.sensitiveWordService = sensitiveWordService;
        this.novelMapper = novelMapper;
        this.userAvatarMapper = userAvatarMapper;
    }

    @Override
    @Transactional
    public Result addComment(CommentDTO commentDTO) {
        //本地敏感词审核
        AuditResultVO auditResultVO = sensitiveWordService.auditText(commentDTO.getContent(), true);
        if(!auditResultVO.getPassed()){
            //拒绝高危敏感词
            return Result.buildResult(BizCodeEnum.SENSITIVE_WORD);
        }

        log.info("发表评论：userId={}, novelId={}, targetType={}",
                commentDTO.getUserId(), commentDTO.getNovelId(), commentDTO.getTargetType());


        Comment comment = new Comment();

        BeanUtils.copyProperties(commentDTO, comment, "id");
        comment.setReplyCount(0);
        // 默认未审核
        comment.setAuditLevel(0);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());

        // 处理回复评论的情况
        if (commentDTO.getParentId() != null) {
            // 增加父评论的回复数
            commentMapper.incrementReplyCount(commentDTO.getParentId());
        }

        // 先插入评论，获取生成的ID
        int result = commentMapper.insert(comment);
        if (result != 1) {
            log.error("评论发表失败：userId={}", commentDTO.getUserId());
            return Result.buildResult(BizCodeEnum.FAIL);
        }

        log.info("评论发表成功：commentId={}", comment.getId());


        // 使用生成的评论ID进行AI审核
        Result aiAuditResult = aiAuditService.auditWithAi(commentDTO.getContent(), comment.getId(), AuditAimTypeEnum.COMMENT.getValue(), auditResultVO);
        if(aiAuditResult.getCode() != BizCodeEnum.SUCCESS.getCode()){
            //高危敏感词在auditWithAi就被拦截并返回最终结果了,这里返回这个枚举类就行了
            return Result.buildResult(BizCodeEnum.SENSITIVE_WORD);
        }

        CommentVO vo = convertToVO(comment);
        return Result.success(vo);
    }

    @Override
    @Transactional
    public Result deleteComment(Long commentId, Long userId) {
        log.info("删除评论：commentId={}, userId={}", commentId, userId);

        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            log.warn("评论不存在：commentId={}", commentId);
            return Result.buildResult(BizCodeEnum.RESOURCE_NOT_FOUND);
        }

        // 权限校验：只能删除自己的评论
        if (!comment.getUserId().equals(userId)) {
            log.warn("无权删除评论：commentId={}, userId={}", commentId, userId);
            return Result.error("无权删除此评论");
        }

        // 如果是回复评论，减少父评论的回复数
        if (comment.getParentId() != null) {
            commentMapper.decrementReplyCount(comment.getParentId());
        }

        int result = commentMapper.deleteById(commentId);
        if (result == 1) {
            log.info("评论删除成功：commentId={}", commentId);
            return Result.success("删除成功");
        } else {
            log.error("评论删除失败：commentId={}", commentId);
            return Result.error("删除失败");
        }
    }

    @Override
    public Result updateComment(CommentDTO commentDTO) {
        log.info("更新评论：commentId={}", commentDTO.getId());

        Comment existingComment = commentMapper.selectById(commentDTO.getId());
        if (existingComment == null) {
            log.warn("评论不存在：commentId={}", commentDTO.getId());
            return Result.buildResult(BizCodeEnum.RESOURCE_NOT_FOUND);
        }

        // 权限校验：只能修改自己的评论
        if (!existingComment.getUserId().equals(commentDTO.getUserId())) {
            log.warn("无权修改评论：commentId={}, userId={}", commentDTO.getId(), commentDTO.getUserId());
            return Result.error("无权修改此评论");
        }

        // 只更新评论内容
        existingComment.setContent(commentDTO.getContent());
        existingComment.setUpdateTime(LocalDateTime.now());
        // 更新后重置审核状态
        existingComment.setAuditLevel(0);

        int result = commentMapper.update(existingComment);
        if (result == 1) {
            log.info("评论更新成功：commentId={}", commentDTO.getId());
            return Result.success("更新成功");
        } else {
            log.error("评论更新失败：commentId={}", commentDTO.getId());
            return Result.error("更新失败");
        }
    }

    @Override
    public Result getCommentById(Long commentId) {
        log.info("获取评论详情：commentId={}", commentId);

        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            log.warn("评论不存在：commentId={}", commentId);
            return Result.buildResult(BizCodeEnum.RESOURCE_NOT_FOUND);
        }

        CommentVO vo = convertToVO(comment);
        return Result.success(vo);
    }

    @Override
    public Result getCommentList(CommentQueryDTO queryDTO) {
        log.info("分页查询评论列表：queryDTO={}", queryDTO);

        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        if (queryDTO.getNovelId() != null) {
            queryWrapper.eq(Comment::getNovelId, queryDTO.getNovelId());
        }
        if (queryDTO.getTargetType() != null) {
            queryWrapper.eq(Comment::getTargetType, queryDTO.getTargetType());
        }
        if (queryDTO.getTargetId() != null) {
            queryWrapper.eq(Comment::getTargetId, queryDTO.getTargetId());
        }
        if (queryDTO.getUserId() != null) {
            queryWrapper.eq(Comment::getUserId, queryDTO.getUserId());
        }
        if (queryDTO.getUserType() != null) {
            queryWrapper.eq(Comment::getUserType, queryDTO.getUserType());
        }
        if (queryDTO.getUserName() != null && !queryDTO.getUserName().isEmpty()) {
            queryWrapper.like(Comment::getUserName, queryDTO.getUserName());
        }
        if (queryDTO.getContent() != null && !queryDTO.getContent().isEmpty()) {
            queryWrapper.like(Comment::getContent, queryDTO.getContent());
        }
        if (queryDTO.getParentId() != null) {
            queryWrapper.eq(Comment::getParentId, queryDTO.getParentId());
        }
        if (queryDTO.getRootId() != null) {
            queryWrapper.eq(Comment::getRootId, queryDTO.getRootId());
        }
        if (queryDTO.getAuditLevel() != null) {
            queryWrapper.eq(Comment::getAuditLevel, queryDTO.getAuditLevel());
        }

        // 管理端查询：查询所有评论（包括一级评论和回复）
        // 普通查询：只查询一级评论（parentId为空）
        // 判断依据：如果明确指定了 parentId 或 rootId，则按指定条件查询
        // 否则默认查询所有评论（管理端行为）
        
        // 按创建时间倒序
        queryWrapper.orderByDesc(Comment::getCreateTime);

        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;
        
        Page<Comment> page = new Page<>(pageNum, pageSize);
        Page<Comment> resultPage = commentMapper.selectPage(page, queryWrapper);

        // 转换为VO
        List<CommentVO> voList = new ArrayList<>();
        for (Comment comment : resultPage.getRecords()) {
            CommentVO vo = convertToVO(comment);
            voList.add(vo);
        }

        PageResult<CommentVO> pageResult = PageResult.build(
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize(),
                resultPage.getTotal(),
                voList
        );

        return Result.success(pageResult);
    }

    @Override
    public Result getNovelComments(Long novelId, Integer pageNum, Integer pageSize) {
        log.info("获取小说评论列表：novelId={}", novelId);

        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getNovelId, novelId)
                .isNull(Comment::getParentId) // 只查询一级评论
                .orderByDesc(Comment::getCreateTime);

        Page<Comment> page = new Page<>(pageNum, pageSize);
        Page<Comment> resultPage = commentMapper.selectPage(page, queryWrapper);

        List<CommentVO> voList = new ArrayList<>();
        for (Comment comment : resultPage.getRecords()) {
            CommentVO vo = convertToVO(comment);
            voList.add(vo);
        }

        PageResult<CommentVO> pageResult = PageResult.build(
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize(),
                resultPage.getTotal(),
                voList
        );

        return Result.success(pageResult);
    }

    @Override
    public Result getCommentReplies(Long rootId, Integer pageNum, Integer pageSize) {
        log.info("获取评论回复列表：rootId={}", rootId);

        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getRootId, rootId)
                .isNotNull(Comment::getParentId)
                .orderByAsc(Comment::getCreateTime);

        Page<Comment> page = new Page<>(pageNum, pageSize);
        Page<Comment> resultPage = commentMapper.selectPage(page, queryWrapper);

        List<CommentVO> voList = new ArrayList<>();
        for (Comment comment : resultPage.getRecords()) {
            CommentVO vo = convertToVO(comment);
            voList.add(vo);
        }

        PageResult<CommentVO> pageResult = PageResult.build(
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize(),
                resultPage.getTotal(),
                voList
        );

        return Result.success(pageResult);
    }

    @Override
    public Result getMyComments(Long userId, Integer userType, Integer pageNum, Integer pageSize) {
        log.info("获取我的评论：userId={}, userType={}", userId, userType);

        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getUserId, userId)
                .eq(Comment::getUserType, userType)
                .orderByDesc(Comment::getCreateTime);

        Page<Comment> page = new Page<>(pageNum, pageSize);
        Page<Comment> resultPage = commentMapper.selectPage(page, queryWrapper);

        List<CommentVO> voList = new ArrayList<>();
        for (Comment comment : resultPage.getRecords()) {
            CommentVO vo = convertToVO(comment);
            voList.add(vo);
        }

        PageResult<CommentVO> pageResult = PageResult.build(
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize(),
                resultPage.getTotal(),
                voList
        );

        return Result.success(pageResult);
    }

    @Override
    @Transactional
    public Result auditComment(Long commentId, Integer auditLevel) {
        log.info("审核评论：commentId={}, auditLevel={}", commentId, auditLevel);

        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            log.warn("评论不存在：commentId={}", commentId);
            return Result.buildResult(BizCodeEnum.RESOURCE_NOT_FOUND);
        }

        comment.setAuditLevel(auditLevel);
        comment.setUpdateTime(LocalDateTime.now());

        int result = commentMapper.update(comment);
        if (result == 1) {
            log.info("审核成功：commentId={}", commentId);
            return Result.success("审核成功");
        } else {
            log.error("审核失败：commentId={}", commentId);
            return Result.error("审核失败");
        }
    }

    /**
     * 转换为VO
     */
    private CommentVO convertToVO(Comment comment) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);
        vo.setUserTypeName(getUserTypeName(comment.getUserType()));
        vo.setTargetTypeName(getTargetTypeName(comment.getTargetType()));
        vo.setAuditLevelName(getAuditLevelName(comment.getAuditLevel()));
        
        // 如果头像为空，根据用户类型查询头像
        if (comment.getUserAvatar() == null || comment.getUserAvatar().isEmpty()) {
            String avatar = getUserAvatar(comment.getUserId(), comment.getUserType());
            vo.setUserAvatar(avatar);
        }
        
        // 查询小说作者ID
        if (comment.getNovelId() != null) {
            Integer authorId = novelMapper.selectAuthorIdById(comment.getNovelId());
            if (authorId != null) {
                vo.setNovelAuthorId(authorId.longValue());
            }
        }
        
        return vo;
    }

    /**
     * 根据用户ID和类型查询头像
     */
    private String getUserAvatar(Long userId, Integer userType) {
        if (userId == null || userType == null) {
            return null;
        }
        
        String avatar = null;
        switch (userType) {
            case 1: // 访客
                avatar = userAvatarMapper.selectVisitorAvatarById(userId);
                break;
            case 2: // 作者
                avatar = userAvatarMapper.selectAuthorAvatarById(userId);
                break;
            case 3: // 管理员
                avatar = userAvatarMapper.selectManagerAvatarById(userId);
                break;
        }
        
        return avatar;
    }

    /**
     * 获取用户类型名称
     */
    private String getUserTypeName(Integer userType) {
        if (userType == null) {
            return "未知";
        }
        return switch (userType) {
            case 1 -> "访客";
            case 2 -> "作者";
            case 3 -> "管理员";
            default -> "未知";
        };
    }

    /**
     * 获取评论对象类型名称
     */
    private String getTargetTypeName(Integer targetType) {
        if (targetType == null) {
            return "未知";
        }
        return switch (targetType) {
            case 1 -> "小说";
            case 2 -> "章节";
            default -> "未知";
        };
    }

    /**
     * 获取审核层级名称
     */
    private String getAuditLevelName(Integer auditLevel) {
        if (auditLevel == null) {
            return "未审核";
        }
        return switch (auditLevel) {
            case 0 -> "未审核";
            case 1 -> "本地过滤通过";
            case 2 -> "人工审核通过";
            default -> "未知";
        };
    }

    @Override
    public Result getNovelCommentTree(Long novelId, Integer targetType, Integer pageNum, Integer pageSize) {
        log.info("获取小说评论树：novelId={}, targetType={}", novelId, targetType);

        // 查询一级评论
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getNovelId, novelId)
                .isNull(Comment::getParentId); // 只查询一级评论
        
        // 按评论对象类型筛选
        if (targetType != null) {
            queryWrapper.eq(Comment::getTargetType, targetType);
        }
        
        queryWrapper.orderByDesc(Comment::getCreateTime);

        Page<Comment> page = new Page<>(pageNum, pageSize);
        Page<Comment> resultPage = commentMapper.selectPage(page, queryWrapper);

        // 转换为VO并填充回复
        List<CommentVO> voList = new ArrayList<>();
        for (Comment comment : resultPage.getRecords()) {
            CommentVO vo = convertToVO(comment);
            
            // 查询该评论的所有回复
            if (comment.getReplyCount() != null && comment.getReplyCount() > 0) {
                List<CommentVO> replies = getRepliesForRoot(comment.getId());
                vo.setReplies(replies);
            }
            
            voList.add(vo);
        }

        PageResult<CommentVO> pageResult = PageResult.build(
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize(),
                resultPage.getTotal(),
                voList
        );

        return Result.success(pageResult);
    }

    /**
     * 获取某条根评论的所有回复（递归获取）
     */
    private List<CommentVO> getRepliesForRoot(Long rootId) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getRootId, rootId)
                .isNotNull(Comment::getParentId)
                .orderByAsc(Comment::getCreateTime);

        List<Comment> replies = commentMapper.selectList(queryWrapper);
        
        List<CommentVO> voList = new ArrayList<>();
        for (Comment reply : replies) {
            CommentVO vo = convertToVO(reply);
            voList.add(vo);
        }

        return voList;
    }

    // ==================== 管理端方法实现 ====================

    @Override
    @Transactional
    public Result managerDeleteComment(Long commentId) {
        log.info("管理员删除评论：commentId={}", commentId);

        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            log.warn("评论不存在：commentId={}", commentId);
            return Result.error("评论不存在");
        }

        // 如果有父评论，减少父评论的回复数
        if (comment.getParentId() != null) {
            commentMapper.decrementReplyCount(comment.getParentId());
        }

        int result = commentMapper.deleteById(commentId);
        if (result == 1) {
            log.info("管理员删除评论成功：commentId={}", commentId);
            return Result.success("删除成功");
        } else {
            log.error("管理员删除评论失败：commentId={}", commentId);
            return Result.error("删除失败");
        }
    }

    @Override
    @Transactional
    public Result managerBatchDeleteComment(List<Long> ids) {
        log.info("管理员批量删除评论：ids={}", ids);

        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要删除的评论");
        }

        int count = 0;
        for (Long id : ids) {
            Comment comment = commentMapper.selectById(id);
            if (comment != null) {
                // 减少父评论回复数
                if (comment.getParentId() != null) {
                    commentMapper.decrementReplyCount(comment.getParentId());
                }
                commentMapper.deleteById(id);
                count++;
            }
        }

        log.info("管理员批量删除评论成功：删除{}条", count);
        return Result.success("成功删除" + count + "条评论");
    }

    @Override
    @Transactional
    public Result managerBatchAuditComment(List<Long> ids, Integer auditLevel) {
        log.info("管理员批量审核评论：ids={}, auditLevel={}", ids, auditLevel);

        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要审核的评论");
        }

        int count = 0;
        for (Long id : ids) {
            Comment comment = commentMapper.selectById(id);
            if (comment != null) {
                comment.setAuditLevel(auditLevel);
                comment.setUpdateTime(LocalDateTime.now());
                commentMapper.update(comment);
                count++;
            }
        }

        log.info("管理员批量审核评论成功：审核{}条", count);
        return Result.success("成功审核" + count + "条评论");
    }
}