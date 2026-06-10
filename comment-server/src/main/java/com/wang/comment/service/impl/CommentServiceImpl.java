package com.wang.comment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.enums.AuditAimTypeEnum;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import org.springframework.beans.BeanUtils;
import com.wang.common.feign.AuthorServiceFeign;
import com.wang.common.feign.VisitorServiceFeign;
import com.wang.common.feign.ManagerServiceFeign;
import com.wang.common.feign.NovelServiceFeign;
import com.wang.common.feign.AiAuditServiceFeign;
import com.wang.common.feign.SensitiveWordServiceFeign;
import com.wang.comment.mapper.CommentMapper;
import com.wang.comment.service.CommentService;
import com.wang.pojo.dto.AiCommentAuditDTO;
import com.wang.pojo.dto.CommentDTO;
import com.wang.pojo.dto.CommentQueryDTO;
import com.wang.pojo.entity.Comment;
import com.wang.pojo.vo.AuditResultVO;
import com.wang.pojo.vo.CommentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 */
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final AiAuditServiceFeign aiAuditServiceFeign;
    private final SensitiveWordServiceFeign sensitiveWordServiceFeign;
    private final NovelServiceFeign novelServiceFeign;
    private final AuthorServiceFeign authorServiceFeign;
    private final VisitorServiceFeign visitorServiceFeign;
    private final ManagerServiceFeign managerServiceFeign;
    private final TransactionTemplate transactionTemplate;

    public CommentServiceImpl(CommentMapper commentMapper, AiAuditServiceFeign aiAuditServiceFeign,
                              SensitiveWordServiceFeign sensitiveWordServiceFeign, NovelServiceFeign novelServiceFeign,
                              AuthorServiceFeign authorServiceFeign, VisitorServiceFeign visitorServiceFeign,
                              ManagerServiceFeign managerServiceFeign, PlatformTransactionManager transactionManager) {
        this.commentMapper = commentMapper;
        this.aiAuditServiceFeign = aiAuditServiceFeign;
        this.sensitiveWordServiceFeign = sensitiveWordServiceFeign;
        this.novelServiceFeign = novelServiceFeign;
        this.authorServiceFeign = authorServiceFeign;
        this.visitorServiceFeign = visitorServiceFeign;
        this.managerServiceFeign = managerServiceFeign;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public Result addComment(CommentDTO commentDTO) {
        // 本地敏感词审核（事务外执行，避免长事务持有数据库连接）
        AuditResultVO auditResultVO = auditContent(commentDTO.getContent());
        if (auditResultVO == null) {
            return Result.buildResult(BizCodeEnum.SYSTEM_ERROR);
        }
        if (Boolean.FALSE.equals(auditResultVO.getPassed())) {
            return Result.buildResult(BizCodeEnum.HIGH_RISK_SENSITIVE_WORD);
        }

        log.info("发表评论：userId={}, novelId={}, targetType={}",
                commentDTO.getUserId(), commentDTO.getNovelId(), commentDTO.getTargetType());

        // 校验父评论是否存在
        if (commentDTO.getParentId() != null) {
            Comment parentComment = commentMapper.selectById(commentDTO.getParentId());
            if (parentComment == null) {
                log.warn("父评论不存在：parentId={}", commentDTO.getParentId());
                return Result.buildResult(BizCodeEnum.RESOURCE_NOT_FOUND);
            }
        }

        Comment comment = new Comment();
        BeanUtils.copyProperties(commentDTO, comment, "id");
        comment.setReplyCount(0);

        // 根据本地审核结果设置 audit_level
        // result=1: 无敏感词，直接通过
        // result=2: 有低危敏感词，需要人工审核
        if (Integer.valueOf(1).equals(auditResultVO.getResult())) {
            comment.setAuditLevel(1);
        } else {
            comment.setAuditLevel(0);
        }

        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());

        // 事务内执行数据库操作
        Integer result;
        try {
            result = transactionTemplate.execute(status -> {
                // 处理回复评论的情况
                if (commentDTO.getParentId() != null) {
                    commentMapper.incrementReplyCount(commentDTO.getParentId());
                }
                return commentMapper.insert(comment);
            });
        } catch (Exception e) {
            log.error("评论发表失败：userId={}", commentDTO.getUserId(), e);
            return Result.buildResult(BizCodeEnum.FAIL);
        }

        if (result == null || result != 1) {
            log.error("评论发表失败：userId={}", commentDTO.getUserId());
            return Result.buildResult(BizCodeEnum.FAIL);
        }

        log.info("评论发表成功：commentId={}, auditLevel={}", comment.getId(), comment.getAuditLevel());

        // 只有存在低危敏感词时才调用AI审核（事务外执行）
        if (Integer.valueOf(2).equals(auditResultVO.getResult())) {
            AiCommentAuditDTO aiAuditDTO = new AiCommentAuditDTO();
            aiAuditDTO.setContent(commentDTO.getContent());
            aiAuditDTO.setAimId(comment.getId());
            aiAuditDTO.setAimType(AuditAimTypeEnum.COMMENT.getValue());
            aiAuditDTO.setLocalResult(auditResultVO);

            try {
                Result aiAuditResult = aiAuditServiceFeign.auditWithAi(aiAuditDTO);
                if (aiAuditResult != null && aiAuditResult.getCode() != BizCodeEnum.SUCCESS.getCode()) {
                    // AI审核发现敏感词，回滚已保存的评论
                    log.warn("AI审核发现敏感词，删除已保存的评论：commentId={}", comment.getId());
                    transactionTemplate.executeWithoutResult(status -> {
                        if (commentDTO.getParentId() != null) {
                            commentMapper.decrementReplyCount(commentDTO.getParentId());
                        }
                        commentMapper.deleteById(comment.getId());
                    });
                    return Result.buildResult(BizCodeEnum.SENSITIVE_WORD);
                }
            } catch (Exception e) {
                log.warn("AI审核服务调用失败，评论已保存等待人工审核：commentId={}", comment.getId(), e);
            }
        }

        // 单条评论使用单条查询
        CommentVO vo = convertToVOSingle(comment);
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
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        // 级联删除子回复
        deleteChildReplies(commentId);

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
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        // 敏感词审核（事务外执行）
        AuditResultVO auditResultVO = auditContent(commentDTO.getContent());
        if (auditResultVO == null) {
            return Result.buildResult(BizCodeEnum.SYSTEM_ERROR);
        }
        if (Boolean.FALSE.equals(auditResultVO.getPassed())) {
            return Result.buildResult(BizCodeEnum.HIGH_RISK_SENSITIVE_WORD);
        }

        // 保存原始内容，AI审核拒绝时恢复
        String originalContent = existingComment.getContent();
        Integer originalAuditLevel = existingComment.getAuditLevel();

        // 只更新评论内容
        existingComment.setContent(commentDTO.getContent());
        existingComment.setUpdateTime(LocalDateTime.now());
        // 更新后根据审核结果重置审核状态
        if (Integer.valueOf(1).equals(auditResultVO.getResult())) {
            existingComment.setAuditLevel(1);
        } else {
            existingComment.setAuditLevel(0);
        }

        // 事务内执行数据库操作
        Integer result;
        try {
            result = transactionTemplate.execute(status -> commentMapper.updateSelective(existingComment));
        } catch (Exception e) {
            log.error("评论更新失败：commentId={}", commentDTO.getId(), e);
            return Result.buildResult(BizCodeEnum.FAIL);
        }

        if (result == null || result != 1) {
            log.error("评论更新失败：commentId={}", commentDTO.getId());
            return Result.buildResult(BizCodeEnum.FAIL);
        }

        log.info("评论更新成功：commentId={}", commentDTO.getId());

        // 低危敏感词调用AI审核（事务外执行）
        if (Integer.valueOf(2).equals(auditResultVO.getResult())) {
            AiCommentAuditDTO aiAuditDTO = new AiCommentAuditDTO();
            aiAuditDTO.setContent(commentDTO.getContent());
            aiAuditDTO.setAimId(existingComment.getId());
            aiAuditDTO.setAimType(AuditAimTypeEnum.COMMENT.getValue());
            aiAuditDTO.setLocalResult(auditResultVO);
            try {
                Result aiAuditResult = aiAuditServiceFeign.auditWithAi(aiAuditDTO);
                if (aiAuditResult != null && aiAuditResult.getCode() != BizCodeEnum.SUCCESS.getCode()) {
                    // AI审核发现敏感词，恢复原始内容
                    log.warn("AI审核发现敏感词，恢复评论原始内容：commentId={}", existingComment.getId());
                    existingComment.setContent(originalContent);
                    existingComment.setAuditLevel(originalAuditLevel);
                    existingComment.setUpdateTime(LocalDateTime.now());
                    transactionTemplate.executeWithoutResult(status -> commentMapper.updateSelective(existingComment));
                    return Result.buildResult(BizCodeEnum.SENSITIVE_WORD);
                }
            } catch (Exception e) {
                log.warn("AI审核服务调用失败，评论已保存等待人工审核：commentId={}", existingComment.getId(), e);
            }
        }

        return Result.success("更新成功");
    }

    @Override
    public Result getCommentById(Long commentId) {
        log.info("获取评论详情：commentId={}", commentId);

        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            log.warn("评论不存在：commentId={}", commentId);
            return Result.buildResult(BizCodeEnum.RESOURCE_NOT_FOUND);
        }

        // 单条评论使用单条查询
        CommentVO vo = convertToVOSingle(comment);
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

        // 管理员评论置顶，然后按创建时间倒序
        queryWrapper.orderByDesc(Comment::getUserType);
        queryWrapper.orderByDesc(Comment::getCreateTime);

        Page<Comment> page = new Page<>(safePageNum(queryDTO.getPageNum()), safePageSize(queryDTO.getPageSize()));
        Page<Comment> resultPage = commentMapper.selectPage(page, queryWrapper);

        List<CommentVO> voList = batchConvertToVO(resultPage.getRecords());

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
                .gt(Comment::getAuditLevel, 0) // 只显示审核通过的评论
                .orderByDesc(Comment::getUserType) // 管理员评论置顶
                .orderByDesc(Comment::getCreateTime);

        Page<Comment> page = new Page<>(safePageNum(pageNum), safePageSize(pageSize));
        Page<Comment> resultPage = commentMapper.selectPage(page, queryWrapper);

        List<CommentVO> voList = batchConvertToVO(resultPage.getRecords());

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
                .gt(Comment::getAuditLevel, 0) // 只显示审核通过的回复
                .orderByAsc(Comment::getCreateTime);

        Page<Comment> page = new Page<>(safePageNum(pageNum), safePageSize(pageSize));
        Page<Comment> resultPage = commentMapper.selectPage(page, queryWrapper);

        List<CommentVO> voList = batchConvertToVO(resultPage.getRecords());

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

        Page<Comment> page = new Page<>(safePageNum(pageNum), safePageSize(pageSize));
        Page<Comment> resultPage = commentMapper.selectPage(page, queryWrapper);

        List<CommentVO> voList = batchConvertToVO(resultPage.getRecords());

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

        // 校验审核级别合法性
        if (auditLevel == null || auditLevel < 0 || auditLevel > 2) {
            log.warn("审核级别不合法：auditLevel={}", auditLevel);
            return Result.buildResult(BizCodeEnum.PARAM_INVALID);
        }

        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            log.warn("评论不存在：commentId={}", commentId);
            return Result.buildResult(BizCodeEnum.RESOURCE_NOT_FOUND);
        }

        comment.setAuditLevel(auditLevel);
        comment.setUpdateTime(LocalDateTime.now());

        int result = commentMapper.updateSelective(comment);
        if (result == 1) {
            log.info("审核成功：commentId={}", commentId);
            return Result.success("审核成功");
        } else {
            log.error("审核失败：commentId={}", commentId);
            return Result.error("审核失败");
        }
    }

    /**
     * 批量转换Comment列表为VO列表（解决N+1查询问题）
     * 先收集所有需要查询的userId和novelId，批量查询后统一填充
     */
    private List<CommentVO> batchConvertToVO(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }

        // 收集需要查询头像的userId（按userType分组）
        Set<Long> visitorIds = new HashSet<>();
        Set<Long> authorIds = new HashSet<>();
        Set<Long> managerIds = new HashSet<>();
        Set<Long> novelIds = new HashSet<>();

        for (Comment comment : comments) {
            if (comment.getUserAvatar() == null || comment.getUserAvatar().isEmpty()) {
                if (comment.getUserType() != null && comment.getUserId() != null) {
                    switch (comment.getUserType()) {
                        case 1 -> visitorIds.add(comment.getUserId());
                        case 2 -> authorIds.add(comment.getUserId());
                        case 3 -> managerIds.add(comment.getUserId());
                    }
                }
            }
            if (comment.getNovelId() != null) {
                novelIds.add(comment.getNovelId());
            }
        }

        // 批量查询头像
        Map<String, String> visitorAvatarMap = batchFetchVisitorAvatars(visitorIds);
        Map<String, String> authorAvatarMap = batchFetchAuthorAvatars(authorIds);
        Map<String, String> managerAvatarMap = batchFetchManagerAvatars(managerIds);
        // 批量查询小说作者ID
        Map<String, Long> novelAuthorMap = batchFetchNovelAuthorIds(novelIds);

        // 转换VO并填充批量查询结果
        List<CommentVO> voList = new ArrayList<>();
        for (Comment comment : comments) {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(comment, vo);
            vo.setUserTypeName(getUserTypeName(comment.getUserType()));
            vo.setTargetTypeName(getTargetTypeName(comment.getTargetType()));
            vo.setAuditLevelName(getAuditLevelName(comment.getAuditLevel()));

            // 从批量结果中填充头像
            if (comment.getUserAvatar() != null && !comment.getUserAvatar().isEmpty()) {
                vo.setUserAvatar(comment.getUserAvatar());
            } else if (comment.getUserType() != null && comment.getUserId() != null) {
                String avatar = switch (comment.getUserType()) {
                    case 1 -> visitorAvatarMap.get(String.valueOf(comment.getUserId()));
                    case 2 -> authorAvatarMap.get(String.valueOf(comment.getUserId()));
                    case 3 -> managerAvatarMap.get(String.valueOf(comment.getUserId()));
                    default -> null;
                };
                vo.setUserAvatar(avatar);
            }

            // 从批量结果中填充小说作者ID
            if (comment.getNovelId() != null) {
                Long authorId = novelAuthorMap.get(String.valueOf(comment.getNovelId()));
                if (authorId != null) {
                    vo.setNovelAuthorId(authorId);
                }
            }

            voList.add(vo);
        }

        return voList;
    }

    /**
     * 单条评论转换为VO（用于单条查询场景）
     */
    private CommentVO convertToVOSingle(Comment comment) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);
        vo.setUserTypeName(getUserTypeName(comment.getUserType()));
        vo.setTargetTypeName(getTargetTypeName(comment.getTargetType()));
        vo.setAuditLevelName(getAuditLevelName(comment.getAuditLevel()));

        // 如果头像为空，通过Feign调用对应服务查询头像
        if (comment.getUserAvatar() == null || comment.getUserAvatar().isEmpty()) {
            String avatar = getUserAvatar(comment.getUserId(), comment.getUserType());
            vo.setUserAvatar(avatar);
        }

        // 通过Feign调用novel-server查询小说作者ID
        if (comment.getNovelId() != null) {
            try {
                Result authorIdResult = novelServiceFeign.getNovelAuthorId(comment.getNovelId());
                if (authorIdResult.getCode() == BizCodeEnum.SUCCESS.getCode() && authorIdResult.getData() instanceof Number) {
                    vo.setNovelAuthorId(((Number) authorIdResult.getData()).longValue());
                }
            } catch (Exception e) {
                log.warn("Feign调用获取小说作者ID失败：novelId={}, error={}", comment.getNovelId(), e.getMessage());
            }
        }

        return vo;
    }

    /**
     * 批量获取访客头像
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> batchFetchVisitorAvatars(Set<Long> visitorIds) {
        if (visitorIds.isEmpty()) {
            return new HashMap<>();
        }
        try {
            Result result = visitorServiceFeign.batchGetVisitorAvatars(new ArrayList<>(visitorIds));
            if (result.getCode() == BizCodeEnum.SUCCESS.getCode() && result.getData() != null) {
                return (Map<String, String>) result.getData();
            }
        } catch (Exception e) {
            log.warn("批量获取访客头像失败：visitorIds={}, error={}", visitorIds, e.getMessage());
        }
        return new HashMap<>();
    }

    /**
     * 批量获取作者头像
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> batchFetchAuthorAvatars(Set<Long> authorIds) {
        if (authorIds.isEmpty()) {
            return new HashMap<>();
        }
        try {
            Result result = authorServiceFeign.batchGetAuthorAvatars(new ArrayList<>(authorIds));
            if (result.getCode() == BizCodeEnum.SUCCESS.getCode() && result.getData() != null) {
                return (Map<String, String>) result.getData();
            }
        } catch (Exception e) {
            log.warn("批量获取作者头像失败：authorIds={}, error={}", authorIds, e.getMessage());
        }
        return new HashMap<>();
    }

    /**
     * 批量获取管理员头像
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> batchFetchManagerAvatars(Set<Long> managerIds) {
        if (managerIds.isEmpty()) {
            return new HashMap<>();
        }
        try {
            Result result = managerServiceFeign.batchGetManagerAvatars(new ArrayList<>(managerIds));
            if (result.getCode() == BizCodeEnum.SUCCESS.getCode() && result.getData() != null) {
                return (Map<String, String>) result.getData();
            }
        } catch (Exception e) {
            log.warn("批量获取管理员头像失败：managerIds={}, error={}", managerIds, e.getMessage());
        }
        return new HashMap<>();
    }

    /**
     * 批量获取小说作者ID
     */
    @SuppressWarnings("unchecked")
    private Map<String, Long> batchFetchNovelAuthorIds(Set<Long> novelIds) {
        if (novelIds.isEmpty()) {
            return new HashMap<>();
        }
        try {
            Result result = novelServiceFeign.batchGetNovelAuthorIds(new ArrayList<>(novelIds));
            if (result.getCode() == BizCodeEnum.SUCCESS.getCode() && result.getData() != null) {
                return (Map<String, Long>) result.getData();
            }
        } catch (Exception e) {
            log.warn("批量获取小说作者ID失败：novelIds={}, error={}", novelIds, e.getMessage());
        }
        return new HashMap<>();
    }

    /**
     * 通过Feign调用对应服务获取用户头像（单条查询，用于单条评论场景）
     */
    private String getUserAvatar(Long userId, Integer userType) {
        if (userId == null || userType == null) {
            return null;
        }

        try {
            String avatar = null;
            switch (userType) {
                case 1: // 访客
                    Result visitorResult = visitorServiceFeign.getVisitorAvatar(userId);
                    if (visitorResult.getCode() == BizCodeEnum.SUCCESS.getCode() && visitorResult.getData() != null) {
                        avatar = (String) visitorResult.getData();
                    }
                    break;
                case 2: // 作者
                    Result authorResult = authorServiceFeign.getAuthorAvatar(userId);
                    if (authorResult.getCode() == BizCodeEnum.SUCCESS.getCode() && authorResult.getData() != null) {
                        avatar = (String) authorResult.getData();
                    }
                    break;
                case 3: // 管理员
                    Result managerResult = managerServiceFeign.getManagerAvatar(userId);
                    if (managerResult.getCode() == BizCodeEnum.SUCCESS.getCode() && managerResult.getData() != null) {
                        avatar = (String) managerResult.getData();
                    }
                    break;
            }
            return avatar;
        } catch (Exception e) {
            log.warn("Feign调用获取用户头像失败：userId={}, userType={}, error={}", userId, userType, e.getMessage());
            return null;
        }
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
                .isNull(Comment::getParentId) // 只查询一级评论
                .gt(Comment::getAuditLevel, 0); // 只显示审核通过的评论

        // 按评论对象类型筛选
        if (targetType != null) {
            queryWrapper.eq(Comment::getTargetType, targetType);
        }

        // 管理员评论置顶，然后按创建时间倒序
        queryWrapper.orderByDesc(Comment::getUserType);
        queryWrapper.orderByDesc(Comment::getCreateTime);

        Page<Comment> page = new Page<>(safePageNum(pageNum), safePageSize(pageSize));
        Page<Comment> resultPage = commentMapper.selectPage(page, queryWrapper);

        // 收集所有评论（一级+回复），统一批量查询头像和作者ID
        List<Comment> allComments = new ArrayList<>(resultPage.getRecords());
        Map<Long, List<Comment>> repliesMap = new HashMap<>();
        for (Comment comment : resultPage.getRecords()) {
            if (comment.getReplyCount() != null && comment.getReplyCount() > 0) {
                List<Comment> replies = getRepliesFromDB(comment.getId());
                repliesMap.put(comment.getId(), replies);
                allComments.addAll(replies);
            }
        }

        // 批量转换所有评论（一级+回复），一次批量查询解决N+1
        List<CommentVO> allVOs = batchConvertToVO(allComments);
        Map<Long, CommentVO> voMap = allVOs.stream()
                .collect(Collectors.toMap(CommentVO::getId, vo -> vo, (v1, v2) -> v1));

        // 组装评论树
        List<CommentVO> voList = new ArrayList<>();
        for (Comment comment : resultPage.getRecords()) {
            CommentVO vo = voMap.get(comment.getId());
            if (vo != null) {
                List<Comment> replies = repliesMap.get(comment.getId());
                if (replies != null && !replies.isEmpty()) {
                    List<CommentVO> replyVOs = replies.stream()
                            .map(r -> voMap.get(r.getId()))
                            .filter(r -> r != null)
                            .collect(Collectors.toList());
                    vo.setReplies(replyVOs);
                }
                voList.add(vo);
            }
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
     * 从数据库查询某条根评论的所有回复
     */
    private List<Comment> getRepliesFromDB(Long rootId) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getRootId, rootId)
                .isNotNull(Comment::getParentId)
                .gt(Comment::getAuditLevel, 0)
                .orderByAsc(Comment::getCreateTime);
        return commentMapper.selectList(queryWrapper);
    }

    /**
     * 迭代删除评论的所有子回复（避免递归栈溢出）
     */
    private void deleteChildReplies(Long commentId) {
        List<Long> toDelete = new ArrayList<>();
        collectChildIds(commentId, toDelete);
        for (Long id : toDelete) {
            commentMapper.deleteById(id);
        }
    }

    /**
     * 广度优先收集所有子回复ID
     */
    private void collectChildIds(Long rootId, List<Long> result) {
        List<Long> currentLevel = List.of(rootId);
        while (!currentLevel.isEmpty()) {
            List<Long> nextLevel = new ArrayList<>();
            for (Long parentId : currentLevel) {
                LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Comment::getParentId, parentId).select(Comment::getId);
                List<Comment> children = commentMapper.selectList(queryWrapper);
                for (Comment child : children) {
                    result.add(child.getId());
                    nextLevel.add(child.getId());
                }
            }
            currentLevel = nextLevel;
        }
    }

    /**
     * 敏感词审核（封装Feign调用，含异常保护和空值检查）
     * @return 审核结果，服务异常时返回null
     */
    private AuditResultVO auditContent(String content) {
        Map<String, String> request = new HashMap<>();
        request.put("content", content);
        try {
            AuditResultVO auditResultVO = (AuditResultVO) sensitiveWordServiceFeign.auditText(request).getData();
            if (auditResultVO == null) {
                log.error("敏感词审核返回空结果");
            }
            return auditResultVO;
        } catch (Exception e) {
            log.error("敏感词审核服务调用失败", e);
            return null;
        }
    }

    /**
     * 安全的分页页码，防止null、负数和过大值
     */
    private int safePageNum(Integer pageNum) {
        if (pageNum == null || pageNum < 1) return 1;
        return Math.min(pageNum, 10000);
    }

    /**
     * 安全的分页大小，防止null、负数和过大值
     */
    private int safePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) return 10;
        return Math.min(pageSize, 100);
    }

    // ==================== 管理端方法实现 ====================

    @Override
    @Transactional
    public Result managerDeleteComment(Long commentId) {
        log.info("管理员删除评论：commentId={}", commentId);

        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            log.warn("评论不存在：commentId={}", commentId);
            return Result.buildResult(BizCodeEnum.RESOURCE_NOT_FOUND);
        }

        // 级联删除子回复
        deleteChildReplies(commentId);

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
            return Result.buildResult(BizCodeEnum.PARAM_INVALID);
        }
        if (ids.size() > 200) {
            return Result.error("单次批量删除不能超过200条");
        }

        int count = 0;
        for (Long id : ids) {
            Comment comment = commentMapper.selectById(id);
            if (comment != null) {
                // 级联删除子回复
                deleteChildReplies(id);
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
            return Result.buildResult(BizCodeEnum.PARAM_INVALID);
        }
        if (ids.size() > 200) {
            return Result.error("单次批量审核不能超过200条");
        }

        // 校验审核级别合法性
        if (auditLevel == null || auditLevel < 0 || auditLevel > 2) {
            log.warn("审核级别不合法：auditLevel={}", auditLevel);
            return Result.buildResult(BizCodeEnum.PARAM_INVALID);
        }

        int count = 0;
        for (Long id : ids) {
            Comment comment = commentMapper.selectById(id);
            if (comment != null) {
                comment.setAuditLevel(auditLevel);
                comment.setUpdateTime(LocalDateTime.now());
                commentMapper.updateSelective(comment);
                count++;
            }
        }

        log.info("管理员批量审核评论成功：审核{}条", count);
        return Result.success("成功审核" + count + "条评论");
    }
}
