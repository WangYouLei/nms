package com.wang.novel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.enums.FileUploadTypeEnum;
import com.wang.common.enums.UserRole;
import com.wang.common.interceptor.LoginInterceptor;
import com.wang.common.model.LoginUser;
import com.wang.common.result.Result;
import com.wang.commonserver.service.FileService;
import com.wang.novel.mapper.NovelChapterMapper;
import com.wang.novel.mapper.NovelMapper;
import com.wang.novel.service.NovelChapterService;
import com.wang.pojo.dto.NovelChapterDTO;
import com.wang.pojo.entity.Novel;
import com.wang.pojo.entity.NovelChapter;
import com.wang.pojo.vo.NovelChapterVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 小说章节服务实现类
 * 提供 author、manager、visitor 三个端口共用的章节功能
 */
@Slf4j
@Service
public class NovelChapterServiceImpl implements NovelChapterService {

    private final NovelChapterMapper novelChapterMapper;
    private final NovelMapper novelMapper;
    private final FileService fileService;

    public NovelChapterServiceImpl(NovelChapterMapper novelChapterMapper,
                                   NovelMapper novelMapper,
                                   FileService fileService) {
        this.novelChapterMapper = novelChapterMapper;
        this.novelMapper = novelMapper;
        this.fileService = fileService;
    }

    // ==================== Common - 公共方法（无权限校验） ====================

    @Override
    public Result getChapterList(Integer novelId) {
        log.info("[Common] 查询章节列表：小说ID={}", novelId);

        LambdaQueryWrapper<NovelChapter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelChapter::getNovelId, novelId)
                .orderByAsc(NovelChapter::getChapterOrder);

        List<NovelChapter> chapterList = novelChapterMapper.selectList(queryWrapper);
        List<NovelChapterVO> voList = chapterList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @Override
    public Result getChapterDetail(Integer chapterId) {
        log.info("[Common] 获取章节详情：章节ID={}", chapterId);

        NovelChapter chapter = novelChapterMapper.selectById(chapterId);
        if (chapter == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_CHAPTER_NOT_FOUND);
        }

        return Result.success(convertToVO(chapter));
    }

    @Override
    public Result getChapterContent(Integer chapterId) {
        log.info("[Common] 获取章节内容：章节ID={}", chapterId);

        NovelChapter chapter = novelChapterMapper.selectById(chapterId);
        if (chapter == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_CHAPTER_NOT_FOUND);
        }

        // 使用 FileService 获取内容
        String content = fileService.getFileContent(chapter.getContentUrl());
        if (content == null) {
            return Result.error("获取章节内容失败");
        }

        NovelChapterVO vo = convertToVO(chapter);
        vo.setContent(content);
        return Result.success(vo);
    }

    // ==================== Author/Manager - 作者/管理端方法（需权限校验） ====================

    @Override
    @Transactional
    public Result uploadChapter(Integer novelId, String title, MultipartFile file) {
        LoginUser loginUser = getLoginUser();
        log.info("[Author] 上传章节：小说ID={}, 章节标题={}, 用户ID={}", novelId, title, loginUser.getId());

        // 权限校验：只有作者可以上传章节
        if (!UserRole.AUTHOR.equals(loginUser.getRole())) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        // 权限校验：检查小说所有权
        Novel novel = checkNovelOwnership(novelId, loginUser.getId());
        if (novel == null) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        // 标题唯一性检查
        if (isTitleExists(novelId, title, null)) {
            return Result.error("章节标题已存在");
        }

        try {
            // 使用 FileService 上传文件
            Result uploadResult = fileService.uploadFile(
                    file,
                    FileUploadTypeEnum.NOVEL_CHAPTER.getCode(),
                    novelId,
                    null
            );

            if (!"success".equals(uploadResult.getMsg())) {
                return Result.error("上传章节文件失败");
            }

            String contentUrl = (String) uploadResult.getData();

            // 保存章节记录
            return saveChapterRecord(novel, title, contentUrl, getNextChapterOrder(novelId));
        } catch (Exception e) {
            log.error("上传章节异常", e);
            return Result.error("上传章节失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result deleteChapter(Integer id) {
        LoginUser loginUser = getLoginUser();
        log.info("[Author/Manager] 删除章节：章节ID={}, 用户ID={}", id, loginUser.getId());

        // 权限校验：作者只能删除自己的章节，管理员可以删除所有章节
        boolean isManager = UserRole.MANAGER.equals(loginUser.getRole());
        boolean isAuthor = UserRole.AUTHOR.equals(loginUser.getRole());
        
        NovelChapter chapter;
        if (isManager) {
            // 管理员只需要检查章节是否存在
            chapter = novelChapterMapper.selectById(id);
            if (chapter == null) {
                return Result.buildResult(BizCodeEnum.NOVEL_CHAPTER_NOT_FOUND);
            }
        } else if (isAuthor) {
            // 作者需要检查章节所有权
            chapter = checkChapterOwnership(id, loginUser.getId());
            if (chapter == null) {
                return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
            }
        } else {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }
        
        Novel novel = novelMapper.selectById(chapter.getNovelId());

        // 删除文件
        fileService.deleteFile(chapter.getContentUrl());

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

    @Override
    @Transactional
    public Result updateChapter(NovelChapterDTO chapterDTO) {
        LoginUser loginUser = getLoginUser();
        log.info("[Author] 更新章节：章节ID={}", chapterDTO.getId());

        // 权限校验：只有作者可以更新章节
        if (!UserRole.AUTHOR.equals(loginUser.getRole())) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        // 权限校验：检查章节所有权
        NovelChapter chapter = checkChapterOwnership(chapterDTO.getId(), loginUser.getId());
        if (chapter == null) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        // 更新标题
        String newTitle = chapterDTO.getTitle();
        if (newTitle != null && !newTitle.equals(chapter.getTitle())) {
            if (isTitleExists(chapter.getNovelId(), newTitle, chapterDTO.getId())) {
                return Result.error("章节标题已存在");
            }
            chapter.setTitle(newTitle);
        }

        // 更新排序
        if (chapterDTO.getChapterOrder() != null) {
            chapter.setChapterOrder(chapterDTO.getChapterOrder());
        }

        // 更新时间
        chapter.setUpdateTime(LocalDateTime.now());

        // 更新数据库
        int result = novelChapterMapper.updateById(chapter);
        if (result == 1) {
            log.info("更新章节成功：章节ID={}", chapter.getId());
            return Result.success(convertToVO(chapter));
        } else {
            log.error("更新章节失败：章节ID={}", chapter.getId());
            return Result.buildResult(BizCodeEnum.FAIL);
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