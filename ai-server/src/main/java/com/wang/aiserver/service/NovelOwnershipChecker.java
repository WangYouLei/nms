package com.wang.aiserver.service;

import com.wang.aiserver.mapper.KnowledgeItemMapper;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.feign.NovelServiceFeign;
import com.wang.common.model.LoginUser;
import com.wang.common.result.Result;
import com.wang.common.utils.RoleContextUtil;
import com.wang.pojo.entity.KnowledgeItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 小说归属校验器
 * 校验当前登录用户对小说（或挂靠小说的知识项）的所有权，防止越权读写他人创作数据
 */
@Slf4j
@Component
public class NovelOwnershipChecker {

    private final NovelServiceFeign novelServiceFeign;
    private final KnowledgeItemMapper knowledgeItemMapper;

    public NovelOwnershipChecker(NovelServiceFeign novelServiceFeign, KnowledgeItemMapper knowledgeItemMapper) {
        this.novelServiceFeign = novelServiceFeign;
        this.knowledgeItemMapper = knowledgeItemMapper;
    }

    /**
     * 校验当前登录用户是否为指定小说的作者
     * @param novelId 小说ID
     * @return 校验通过返回null，否则返回拒绝结果
     */
    public Result checkNovelOwnership(Long novelId) {
        LoginUser current = RoleContextUtil.getCurrentUser();
        if (current == null) {
            return Result.buildResult(BizCodeEnum.USER_NOT_LOGIN);
        }
        if (novelId == null) {
            return Result.error("小说ID不能为空");
        }
        Result authorIdResult = novelServiceFeign.getNovelAuthorId(novelId);
        if (authorIdResult == null
                || !Integer.valueOf(BizCodeEnum.SUCCESS.getCode()).equals(authorIdResult.getCode())
                || authorIdResult.getData() == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_NOT_FOUND);
        }
        Long authorId = Long.valueOf(String.valueOf(authorIdResult.getData()));
        if (!current.getId().equals(authorId)) {
            log.warn("小说归属校验失败：userId={}, novelId={}, novelAuthorId={}", current.getId(), novelId, authorId);
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }
        return null;
    }

    /**
     * 校验当前登录用户对指定知识项（按其所属小说）的所有权
     * @param itemId 知识项ID
     * @return 校验通过返回null，否则返回拒绝结果
     */
    public Result checkItemOwnership(Long itemId) {
        KnowledgeItem item = knowledgeItemMapper.selectById(itemId);
        if (item == null) {
            return Result.buildResult(BizCodeEnum.RESOURCE_NOT_FOUND);
        }
        return checkNovelOwnership(item.getNovelId());
    }
}
