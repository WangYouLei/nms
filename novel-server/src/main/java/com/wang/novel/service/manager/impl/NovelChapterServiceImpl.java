package com.wang.novel.service.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.interceptor.LoginInterceptor;
import com.wang.common.model.LoginUser;
import com.wang.common.result.Result;
import com.wang.novel.mapper.NovelChapterMapper;
import com.wang.novel.mapper.NovelMapper;
import com.wang.novel.service.manager.ChapterStorageService;
import com.wang.novel.service.manager.NovelChapterService;
import com.wang.pojo.dto.NovelChapterDTO;
import com.wang.pojo.entity.Novel;
import com.wang.pojo.entity.NovelChapter;
import com.wang.pojo.vo.NovelChapterVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 小说章节服务实现类
 */
@Slf4j
@Service
public class NovelChapterServiceImpl implements NovelChapterService {

    private final NovelChapterMapper novelChapterMapper;
    private final NovelMapper novelMapper;
    private final ChapterStorageService chapterStorageService;

    public NovelChapterServiceImpl(NovelChapterMapper novelChapterMapper,
                                   NovelMapper novelMapper,
                                   ChapterStorageService chapterStorageService) {
        this.novelChapterMapper = novelChapterMapper;
        this.novelMapper = novelMapper;
        this.chapterStorageService = chapterStorageService;
    }

    // ==================== 公开方法 ====================

    /**
     * 上传章节（通过文件）
     */
    @Override
    @Transactional
    public Result uploadChapter(Integer novelId, String title, MultipartFile file) {
        LoginUser loginUser = getLoginUser();
        log.info("上传章节：小说ID={}, 章节标题={}, 用户ID={}", novelId, title, loginUser.getId());

        // 权限校验
        Novel novel = checkNovelOwnership(novelId, loginUser.getId());
        if (novel == null) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        // 标题唯一性检查
        if (isTitleExists(novelId, title, null)) {
            return Result.error("章节标题已存在");
        }

        try {
            // 上传到 MinIO
            String objectName = chapterStorageService.uploadChapterFile(
                    novelId, title, file.getInputStream(), file.getSize());
            String contentUrl = chapterStorageService.buildContentUrl(objectName);

            // 保存章节记录
            return saveChapterRecord(novel, title, contentUrl, getNextChapterOrder(novelId));
        } catch (IOException e) {
            log.error("读取上传文件失败", e);
            return Result.error("读取上传文件失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("上传章节异常", e);
            return Result.error("上传章节失败：" + e.getMessage());
        }
    }

    /**
     * 保存章节内容（直接保存字符串）
     */
    @Override
    @Transactional
    public Result saveChapterContent(Integer novelId, String title, String content) {
        LoginUser loginUser = getLoginUser();
        log.info("保存章节内容：小说ID={}, 章节标题={}, 用户ID={}", novelId, title, loginUser.getId());

        // 权限校验
        Novel novel = checkNovelOwnership(novelId, loginUser.getId());
        if (novel == null) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        // 标题唯一性检查
        if (isTitleExists(novelId, title, null)) {
            return Result.error("章节标题已存在");
        }

        try {
            // 上传到 MinIO
            String objectName = chapterStorageService.uploadChapterContent(novelId, title, content);
            String contentUrl = chapterStorageService.buildContentUrl(objectName);

            // 保存章节记录
            return saveChapterRecord(novel, title, contentUrl, getNextChapterOrder(novelId));
        } catch (Exception e) {
            log.error("保存章节内容异常", e);
            return Result.error("保存章节内容失败：" + e.getMessage());
        }
    }

    /**
     * 删除章节
     */
    @Override
    @Transactional
    public Result deleteChapter(Integer id) {
        LoginUser loginUser = getLoginUser();
        log.info("删除章节：章节ID={}, 用户ID={}", id, loginUser.getId());

        // 权限校验
        NovelChapter chapter = checkChapterOwnership(id, loginUser.getId());
        if (chapter == null) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }
        Novel novel = novelMapper.selectById(chapter.getNovelId());

        // 删除 MinIO 文件
        String objectName = chapterStorageService.extractObjectName(chapter.getContentUrl());
        chapterStorageService.deleteChapterContent(objectName);

        // 删除数据库记录
        int result = novelChapterMapper.deleteById(id);
        if (result == 1) {
            updateNovelChapterCount(novel, -1);
            log.info("删除章节成功：章节ID={}", id);
            return Result.success(BizCodeEnum.SUCCESS);
        } else {
            log.error("删除章节失败：章节ID={}", id);
            return Result.buildResult(BizCodeEnum.FAIL);
        }
    }

    /**
     * 更新章节信息
     */
    @Override
    @Transactional
    public Result updateChapter(NovelChapterDTO chapterDTO) {
        LoginUser loginUser = getLoginUser();
        log.info("更新章节：章节ID={}", chapterDTO.getId());

        // 权限校验
        NovelChapter chapter = checkChapterOwnership(chapterDTO.getId(), loginUser.getId());
        if (chapter == null) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        // 更新标题
        String newTitle = chapterDTO.getTitle();
        boolean titleChanged = newTitle != null && !newTitle.equals(chapter.getTitle());

        if (titleChanged) {
            if (isTitleExists(chapter.getNovelId(), newTitle, chapterDTO.getId())) {
                return Result.error("章节标题已存在");
            }
            chapter.setTitle(newTitle);
        }

        // 更新排序
        if (chapterDTO.getChapterOrder() != null) {
            chapter.setChapterOrder(chapterDTO.getChapterOrder());
        }

        // 更新内容（MinIO 操作）
        try {
            String objectName = chapterStorageService.extractObjectName(chapter.getContentUrl());

            if (chapterDTO.getContent() != null) {
                // 有新内容：上传新内容
                if (titleChanged) {
                    // 标题变了，需要重命名文件
                    objectName = chapterStorageService.renameChapterContent(
                            objectName, chapter.getNovelId(), newTitle);
                    chapter.setContentUrl(chapterStorageService.buildContentUrl(objectName));
                }
                // 上传新内容覆盖原文件
                chapterStorageService.uploadChapterContent(
                        chapter.getNovelId(), chapter.getTitle(), chapterDTO.getContent());
                log.info("更新章节内容成功：章节ID={}", chapter.getId());
            } else if (titleChanged) {
                // 只有标题变化，重命名文件
                String newObjectName = chapterStorageService.renameChapterContent(
                        objectName, chapter.getNovelId(), newTitle);
                chapter.setContentUrl(chapterStorageService.buildContentUrl(newObjectName));
                log.info("重命名章节文件成功");
            }
        } catch (Exception e) {
            log.error("更新章节内容失败", e);
            return Result.error("更新章节内容失败：" + e.getMessage());
        }

        // 更新数据库
        chapter.setUpdateTime(LocalDateTime.now());
        int result = novelChapterMapper.updateById(chapter);
        if (result == 1) {
            log.info("更新章节成功：章节ID={}", chapter.getId());
            return Result.success(chapter);
        } else {
            log.error("更新章节失败：章节ID={}", chapter.getId());
            return Result.buildResult(BizCodeEnum.FAIL);
        }
    }

    /**
     * 查询章节列表
     */
    @Override
    public Result getChapterList(Integer novelId) {
        LoginUser loginUser = getLoginUser();
        log.info("查询章节列表：小说ID={}, 用户ID={}", novelId, loginUser.getId());

        // 权限校验
        Novel novel = checkNovelOwnership(novelId, loginUser.getId());
        if (novel == null) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        // 查询章节
        LambdaQueryWrapper<NovelChapter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelChapter::getNovelId, novelId)
                .orderByAsc(NovelChapter::getChapterOrder);

        List<NovelChapter> chapterList = novelChapterMapper.selectList(queryWrapper);
        List<NovelChapterVO> voList = chapterList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    /**
     * 获取章节详情
     */
    @Override
    public Result getChapterDetail(Integer id) {
        LoginUser loginUser = getLoginUser();
        log.info("获取章节详情：章节ID={}", id);

        // 权限校验
        NovelChapter chapter = checkChapterOwnership(id, loginUser.getId());
        if (chapter == null) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        return Result.success(convertToVO(chapter));
    }

    /**
     * 获取章节内容
     */
    @Override
    public Result getChapterContent(Integer id) {
        LoginUser loginUser = getLoginUser();
        log.info("获取章节内容：章节ID={}, 用户ID={}", id, loginUser.getId());

        // 权限校验
        NovelChapter chapter = checkChapterOwnership(id, loginUser.getId());
        if (chapter == null) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        try {
            String objectName = chapterStorageService.extractObjectName(chapter.getContentUrl());
            String content = chapterStorageService.getChapterContent(objectName);

            NovelChapterVO vo = convertToVO(chapter);
            vo.setContent(content);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("获取章节内容异常：章节ID={}", id, e);
            return Result.error("获取章节内容失败：" + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 获取当前登录用户
     */
    private LoginUser getLoginUser() {
        return LoginInterceptor.THREAD_LOCAL.get();
    }

    /**
     * 检查小说所有权
     *
     * @return 成功返回 Novel，失败返回 null
     */
    private Novel checkNovelOwnership(Integer novelId, Integer userId) {
        Novel novel = novelMapper.selectById(novelId);
        if (novel == null) {
            return null;
        }
        if (!Objects.equals(novel.getAuthorId(), userId)) {
            return null;
        }
        return novel;
    }

    /**
     * 检查章节所有权（通过章节ID）
     *
     * @return 成功返回 NovelChapter，失败返回 null
     */
    private NovelChapter checkChapterOwnership(Integer chapterId, Integer userId) {
        NovelChapter chapter = novelChapterMapper.selectById(chapterId);
        if (chapter == null) {
            return null;
        }

        Novel novel = novelMapper.selectById(chapter.getNovelId());
        if (novel == null || !Objects.equals(novel.getAuthorId(), userId)) {
            return null;
        }
        return chapter;
    }

    /**
     * 检查章节标题是否已存在
     *
     * @param excludeId 排除的章节ID（更新时使用）
     */
    private boolean isTitleExists(Integer novelId, String title, Integer excludeId) {
        LambdaQueryWrapper<NovelChapter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NovelChapter::getNovelId, novelId)
                .eq(NovelChapter::getTitle, title);
        if (excludeId != null) {
            wrapper.ne(NovelChapter::getId, excludeId);
        }
        return novelChapterMapper.selectCount(wrapper) > 0;
    }

    /**
     * 获取下一个章节序号
     */
    private int getNextChapterOrder(Integer novelId) {
        LambdaQueryWrapper<NovelChapter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NovelChapter::getNovelId, novelId)
                .orderByDesc(NovelChapter::getChapterOrder)
                .last("LIMIT 1");
        NovelChapter lastChapter = novelChapterMapper.selectOne(wrapper);
        return (lastChapter == null) ? 1 : lastChapter.getChapterOrder() + 1;
    }

    /**
     * 保存章节记录到数据库
     */
    private Result saveChapterRecord(Novel novel, String title, String contentUrl, int order) {
        NovelChapter chapter = new NovelChapter();
        chapter.setNovelId(novel.getId());
        chapter.setTitle(title);
        chapter.setContentUrl(contentUrl);
        chapter.setChapterOrder(order);
        chapter.setCreateTime(LocalDateTime.now());
        chapter.setUpdateTime(LocalDateTime.now());

        int result = novelChapterMapper.insert(chapter);
        if (result == 1) {
            updateNovelChapterCount(novel, 1);
            log.info("保存章节成功：章节ID={}", chapter.getId());
            return Result.success(chapter);
        } else {
            log.error("保存章节记录失败");
            return Result.error("保存章节记录失败");
        }
    }

    /**
     * 更新小说章节数
     *
     * @param delta 变化量（+1 或 -1）
     */
    private void updateNovelChapterCount(Novel novel, int delta) {
        int newCount = (novel.getChapterCount() == null ? 0 : novel.getChapterCount()) + delta;
        novel.setChapterCount(Math.max(0, newCount));
        novel.setUpdateTime(LocalDateTime.now());
        novelMapper.updateById(novel);
    }

    /**
     * 转换为 VO
     */
    private NovelChapterVO convertToVO(NovelChapter chapter) {
        NovelChapterVO vo = new NovelChapterVO();
        BeanUtils.copyProperties(chapter, vo);
        return vo;
    }
}