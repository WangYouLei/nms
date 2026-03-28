package com.wang.novel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wang.common.utils.RoleContextUtil;
import com.wang.common.enums.AuditAimTypeEnum;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.enums.FileUploadTypeEnum;
import com.wang.common.enums.UserRole;
import com.wang.common.model.LoginUser;
import com.wang.common.result.Result;
import com.wang.novel.feign.AiAuditServiceFeign;
import com.wang.novel.feign.FileServiceFeign;
import com.wang.novel.feign.SensitiveWordServiceFeign;
import com.wang.novel.mapper.NovelChapterMapper;
import com.wang.novel.mapper.NovelMapper;
import com.wang.novel.service.NovelChapterService;
import com.wang.pojo.entity.Novel;
import com.wang.pojo.entity.NovelChapter;
import com.wang.pojo.vo.AuditResultVO;
import com.wang.pojo.vo.NovelChapterVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final FileServiceFeign fileServiceFeign;
    private final SensitiveWordServiceFeign sensitiveWordServiceFeign;
    private final AiAuditServiceFeign aiAuditServiceFeign;

    public NovelChapterServiceImpl(NovelChapterMapper novelChapterMapper,
                                   NovelMapper novelMapper,
                                   FileServiceFeign fileServiceFeign,
                                   SensitiveWordServiceFeign sensitiveWordServiceFeign,
                                   AiAuditServiceFeign aiAuditServiceFeign) {
        this.novelChapterMapper = novelChapterMapper;
        this.novelMapper = novelMapper;
        this.fileServiceFeign = fileServiceFeign;
        this.sensitiveWordServiceFeign = sensitiveWordServiceFeign;
        this.aiAuditServiceFeign = aiAuditServiceFeign;
    }

    // ==================== Common - 公共方法（无权限校验） ====================

    @Override
    public Result getChapterList(Integer novelId) {
        LambdaQueryWrapper<NovelChapter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelChapter::getNovelId, novelId)
                .orderByAsc(NovelChapter::getChapterOrder);

        List<NovelChapter> chapterList = novelChapterMapper.selectList(queryWrapper);
        List<NovelChapterVO> voList = chapterList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        if(voList.isEmpty()){
            return Result.error("该小说暂无章节");
        }
        return Result.success(voList);
    }

    @Override
    public Result getChapterDetail(Integer chapterId) {
        NovelChapter chapter = novelChapterMapper.selectById(chapterId);
        if (chapter == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_CHAPTER_NOT_FOUND);
        }

        return Result.success(convertToVO(chapter));
    }

    @Override
    public Result getChapterContent(Integer chapterId) {
        NovelChapter chapter = novelChapterMapper.selectById(chapterId);
        if (chapter == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_CHAPTER_NOT_FOUND);
        }

        // 使用 FileServiceFeign 获取内容
        Result contentResult = fileServiceFeign.getFileContent(chapter.getContentUrl());
        String content = (String) contentResult.getData();
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
    public Result uploadChapter(Integer novelId, String title, Integer wordCount, MultipartFile file) {
        LoginUser loginUser = getLoginUser();
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
            // 使用 FileServiceFeign 上传文件
            Result uploadResult = fileServiceFeign.uploadFile(
                    file,
                    FileUploadTypeEnum.NOVEL_CHAPTER.getCode(),
                    novelId,
                    null
            );

            if (uploadResult.getCode() != 10000) {
                return Result.error("上传新章节文件失败");
            }

            String contentUrl = (String) uploadResult.getData();

            // 获取章节内容进行敏感词审核
            Result contentResult = fileServiceFeign.getFileContent(contentUrl);
            String content = (String) contentResult.getData();
            
            if (content != null && !content.isEmpty()) {
                // 本地敏感词审核
                Map<String, String> request = new HashMap<>();
                request.put("content", content);
                AuditResultVO auditResultVO = sensitiveWordServiceFeign.auditText(request);
                if (!auditResultVO.getPassed()) {
                    // 删除已上传的文件
                    fileServiceFeign.deleteFile(contentUrl);
                    // 拒绝高危敏感词
                    return Result.buildResult(BizCodeEnum.SENSITIVE_WORD);
                }

                // AI 审核
                Result aiAuditResult = aiAuditServiceFeign.auditWithAi(content, null, AuditAimTypeEnum.CHAPTER.getValue(), auditResultVO);
                if (aiAuditResult.getCode() != BizCodeEnum.SUCCESS.getCode()) {
                    // 删除已上传的文件
                    fileServiceFeign.deleteFile(contentUrl);
                    return Result.buildResult(BizCodeEnum.SENSITIVE_WORD);
                }
            }

            // 保存章节记录
            return saveChapterRecord(novel, title, contentUrl, getNextChapterOrder(novelId), wordCount);
        } catch (Exception e) {
            log.error("上传新章节异常", e);
            return Result.error("上传新章节失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result deleteChapter(Integer id) {
        LoginUser loginUser = getLoginUser();

        // 权限校验：作者只能删除自己的章节
        boolean isAuthor = UserRole.AUTHOR.equals(loginUser.getRole());
        
        NovelChapter chapter;
        if (isAuthor) {
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
        fileServiceFeign.deleteFile(chapter.getContentUrl());

        // 删除数据库记录
        int result = novelChapterMapper.deleteById(id);
        if (result == 1) {
            // 更新小说章节数和总字数
            updateNovelChapterCount(novel, -1);
            updateNovelAllWordCount(novel, -(chapter.getWordCount() != null ? chapter.getWordCount() : 0));
            log.info("删除章节成功：章节ID={}", id);
            return Result.success(BizCodeEnum.SUCCESS);
        } else {
            log.error("删除章节失败：章节ID={}", id);
            return Result.buildResult(BizCodeEnum.FAIL);
        }
    }

    // 更新章节信息（包括章节内容）
    public Result updateChapter(Integer id, String title, Integer chapterOrder, Integer wordCount, String oldFileUrl, MultipartFile file) {
        LoginUser loginUser = getLoginUser();

        // 权限校验：检查章节所有权
        NovelChapter chapter = checkChapterOwnership(id, loginUser.getId());
        if (chapter == null) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        // 保存旧字数用于计算差值
        Integer oldWordCount = chapter.getWordCount() != null ? chapter.getWordCount() : 0;
        boolean wordCountChanged = false;

        // 更新标题
        if (StringUtils.hasText(title) && !title.equals(chapter.getTitle())) {
            // 检查标题是否重复
            if (isTitleExists(chapter.getNovelId(), title, id)) {
                return Result.error("章节标题已存在");
            }
            chapter.setTitle(title);
        }

        // 更新排序
        if (chapterOrder != null && chapterOrder > 0) {
            chapter.setChapterOrder(chapterOrder);
        }

        // 更新字数
        if (wordCount != null && wordCount >= 0) {
            if (!wordCount.equals(oldWordCount)) {
                wordCountChanged = true;
            }
            chapter.setWordCount(wordCount);
        }

        // 更新章节内容文件（如果提供了新文件）
        if (file != null && !file.isEmpty()) {
            try {
                // 上传新文件
                Result uploadResult = fileServiceFeign.uploadFile(
                        file,
                        FileUploadTypeEnum.NOVEL_CHAPTER.getCode(),
                        chapter.getNovelId(),
                        // 传入旧文件URL，由 FileServiceFeign 处理删除
                        oldFileUrl
                );

                if (uploadResult.getCode() != 10000) {
                    return Result.error("更新章节文件失败");
                }

                // 更新内容URL
                String newContentUrl = (String) uploadResult.getData();

                // 获取章节内容进行敏感词审核
                Result contentResult = fileServiceFeign.getFileContent(newContentUrl);
                String content = (String) contentResult.getData();

                if (content != null && !content.isEmpty()) {
                    // 本地敏感词审核
                    Map<String, String> request = new HashMap<>();
                    request.put("content", content);
                    AuditResultVO auditResultVO = sensitiveWordServiceFeign.auditText(request);
                    if (!auditResultVO.getPassed()) {
                        // 拒绝高危敏感词
                        return Result.buildResult(BizCodeEnum.SENSITIVE_WORD);
                    }

                    // AI 审核
                    Result aiAuditResult = aiAuditServiceFeign.auditWithAi(content, id.longValue(), AuditAimTypeEnum.CHAPTER.getValue(), auditResultVO);
                    if (aiAuditResult.getCode() != BizCodeEnum.SUCCESS.getCode()) {
                        return Result.buildResult(BizCodeEnum.SENSITIVE_WORD);
                    }
                }

                chapter.setContentUrl(newContentUrl);
            } catch (Exception e) {
                log.error("更新章节文件异常：{}", e.getMessage());
                return Result.error("更新章节文件失败：" + e.getMessage());
            }
        }

        // 更新时间
        chapter.setUpdateTime(LocalDateTime.now());

        // 更新数据库
        int result = novelChapterMapper.update(chapter);
        if (result == 1) {
            // 如果字数有变化，更新小说总字数
            if (wordCountChanged && wordCount != null) {
                Novel novel = novelMapper.selectById(chapter.getNovelId());
                int delta = wordCount - oldWordCount;
                updateNovelAllWordCount(novel, delta);
            }
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
        return RoleContextUtil.getCurrentUser();
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
            //忽略掉当前章节
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
    private Result saveChapterRecord(Novel novel, String title, String contentUrl, int order, Integer wordCount) {
        NovelChapter chapter = new NovelChapter();
        chapter.setNovelId(novel.getId());
        chapter.setTitle(title);
        chapter.setContentUrl(contentUrl);
        chapter.setWordCount(wordCount != null ? wordCount : 0);
        chapter.setChapterOrder(order);
        chapter.setCreateTime(LocalDateTime.now());
        chapter.setUpdateTime(LocalDateTime.now());

        int result = novelChapterMapper.insert(chapter);
        if (result == 1) {
            // 更新小说章节数和总字数
            updateNovelChapterCount(novel, 1);
            updateNovelAllWordCount(novel, wordCount != null ? wordCount : 0);
            log.info("保存章节成功：章节ID={}, 字数={}", chapter.getId(), wordCount);
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
        novelMapper.update(novel);
    }

    /**
     * 更新小说总字数
     *
     * @param delta 变化量（正数增加，负数减少）
     */
    private void updateNovelAllWordCount(Novel novel, int delta) {
        int newWordCount = (novel.getAllWordCount() == null ? 0 : novel.getAllWordCount()) + delta;
        novel.setAllWordCount(Math.max(0, newWordCount));
        novel.setUpdateTime(LocalDateTime.now());
        novelMapper.update(novel);
        log.info("更新小说总字数：小说ID={}, 变化量={}, 新总字数={}", novel.getId(), delta, newWordCount);
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