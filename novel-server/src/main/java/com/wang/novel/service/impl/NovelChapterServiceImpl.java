package com.wang.novel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wang.common.utils.RoleContextUtil;
import com.wang.common.enums.AuditAimTypeEnum;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.enums.FileUploadTypeEnum;
import com.wang.common.enums.UserRole;
import com.wang.common.model.LoginUser;
import com.wang.common.result.Result;
import com.wang.common.feign.AiAuditServiceFeign;
import com.wang.common.service.CacheService;
import com.wang.common.constants.CacheConstants;
import com.wang.common.feign.FileServiceFeign;
import com.wang.common.feign.SensitiveWordServiceFeign;
import com.wang.novel.mapper.NovelChapterMapper;
import com.wang.novel.mapper.NovelMapper;
import com.wang.novel.service.NovelChapterService;
import com.wang.pojo.dto.AiCommentAuditDTO;
import com.wang.pojo.entity.Novel;
import com.wang.pojo.entity.NovelChapter;
import com.wang.pojo.vo.AuditResultVO;
import com.wang.pojo.vo.NovelChapterVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final NovelChapterMapper novelChapterMapper;
    private final NovelMapper novelMapper;
    private final FileServiceFeign fileServiceFeign;
    private final SensitiveWordServiceFeign sensitiveWordServiceFeign;
    private final AiAuditServiceFeign aiAuditServiceFeign;
    private final CacheService cacheService;
    private final TransactionTemplate transactionTemplate;
    private final RestTemplate restTemplate;

    public NovelChapterServiceImpl(NovelChapterMapper novelChapterMapper,
                                   NovelMapper novelMapper,
                                   FileServiceFeign fileServiceFeign,
                                   SensitiveWordServiceFeign sensitiveWordServiceFeign,
                                   AiAuditServiceFeign aiAuditServiceFeign,
                                   CacheService cacheService,
                                   PlatformTransactionManager transactionManager,
                                   RestTemplate restTemplate) {
        this.novelChapterMapper = novelChapterMapper;
        this.novelMapper = novelMapper;
        this.fileServiceFeign = fileServiceFeign;
        this.sensitiveWordServiceFeign = sensitiveWordServiceFeign;
        this.aiAuditServiceFeign = aiAuditServiceFeign;
        this.cacheService = cacheService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.restTemplate = restTemplate;
    }

    // ==================== Common - 公共方法（无权限校验） ====================

    @Override
    public Result getChapterList(Long novelId) {
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
    public Result getChapterDetail(Long chapterId) {
        NovelChapter chapter = novelChapterMapper.selectById(chapterId);
        if (chapter == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_CHAPTER_NOT_FOUND);
        }

        return Result.success(convertToVO(chapter));
    }

    @Override
    public Result getChapterContent(Long chapterId) {
        log.info("获取章节内容，章节ID: {}", chapterId);

        NovelChapter chapter = novelChapterMapper.selectById(chapterId);
        if (chapter == null) {
            log.warn("章节不存在，章节ID: {}", chapterId);
            return Result.buildResult(BizCodeEnum.NOVEL_CHAPTER_NOT_FOUND);
        }

        log.info("章节信息: id={}, title={}, contentUrl={}",
                chapter.getId(), chapter.getTitle(), chapter.getContentUrl());

        // 使用 FileServiceFeign 获取内容
        Result contentResult = fileServiceFeign.getFileContent(chapter.getContentUrl());
        log.info("文件服务返回结果: code={}, msg={}", contentResult.getCode(), contentResult.getMsg());

        String content = (String) contentResult.getData();
        if (content == null) {
            log.error("获取章节内容失败，章节ID: {}, contentUrl: {}", chapterId, chapter.getContentUrl());
            return Result.error("获取章节内容失败");
        }

        log.info("成功获取章节内容，章节ID: {}, 内容长度: {}", chapterId, content.length());

        NovelChapterVO vo = convertToVO(chapter);
        vo.setContent(content);
        return Result.success(vo);
    }

    // ==================== Author/Manager - 作者/管理端方法（需权限校验） ====================

    @Override
    public Result uploadChapter(Long novelId, String title, Integer wordCount, MultipartFile file) {
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
            // === 事务外：远程调用（文件上传 + 内容审核） ===
            Result uploadResult = uploadFileToCommonServer(
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
                AuditResultVO auditResultVO = OBJECT_MAPPER.convertValue(sensitiveWordServiceFeign.auditText(request).getData(), AuditResultVO.class);
                if (!auditResultVO.getPassed()) {
                    // 删除已上传的文件
                    fileServiceFeign.deleteFile(contentUrl);
                    return Result.buildResult(BizCodeEnum.SENSITIVE_WORD);
                }

                // AI 审核
                AiCommentAuditDTO aiAuditDTO = new AiCommentAuditDTO();
                aiAuditDTO.setContent(content);
                aiAuditDTO.setAimId(null);
                aiAuditDTO.setAimType(AuditAimTypeEnum.CHAPTER.getValue());
                aiAuditDTO.setLocalResult(auditResultVO);
                Result aiAuditResult = aiAuditServiceFeign.auditWithAi(aiAuditDTO);
                if (aiAuditResult.getCode() != BizCodeEnum.SUCCESS.getCode()) {
                    // 删除已上传的文件
                    fileServiceFeign.deleteFile(contentUrl);
                    return Result.buildResult(BizCodeEnum.AI_AUDIT_ERROR);
                }
            }

            // === 事务内：数据库操作 ===
            final String finalContentUrl = contentUrl;
            return transactionTemplate.execute(status -> {
                // 重新查询 novel，确保事务内数据一致
                Novel freshNovel = novelMapper.selectById(novelId);
                return saveChapterRecord(freshNovel, title, finalContentUrl, getNextChapterOrder(novelId), wordCount);
            });
        } catch (Exception e) {
            log.error("上传新章节异常", e);
            return Result.error("上传新章节失败：" + e.getMessage());
        }
    }

    @Override
    public Result deleteChapter(Long id) {
        LoginUser loginUser = getLoginUser();

        // 权限校验：作者只能删除自己的章节
        boolean isAuthor = UserRole.AUTHOR.equals(loginUser.getRole());

        NovelChapter chapter;
        if (isAuthor) {
            chapter = checkChapterOwnership(id, loginUser.getId());
            if (chapter == null) {
                return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
            }
        } else {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        // === 事务外：Feign 远程调用（删除文件） ===
        fileServiceFeign.deleteFile(chapter.getContentUrl());

        // === 事务内：数据库操作 ===
        Novel novel = novelMapper.selectById(chapter.getNovelId());
        Integer result = transactionTemplate.execute(status -> {
            int rows = novelChapterMapper.deleteById(id);
            if (rows == 1) {
                updateNovelChapterCount(novel, -1);
                updateNovelAllWordCount(novel, -(chapter.getWordCount() != null ? chapter.getWordCount() : 0));
            }
            return rows;
        });

        if (result != null && result == 1) {
            log.info("删除章节成功：章节ID={}", id);
            cacheService.zIncrBy(CacheConstants.RANKING_NOVEL_ONGOING, -1, String.valueOf(novel.getId()));
            cacheService.zAdd(CacheConstants.RANKING_NOVEL_LATEST, System.currentTimeMillis(), String.valueOf(novel.getId()));
            return Result.success(BizCodeEnum.SUCCESS);
        } else {
            log.error("删除章节失败：章节ID={}", id);
            return Result.buildResult(BizCodeEnum.FAIL);
        }
    }

    // 更新章节信息（包括章节内容）
    public Result updateChapter(Long id, String title, Integer chapterOrder, Integer wordCount, String oldFileUrl, MultipartFile file) {
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

        // === 事务外：远程调用（文件上传 + 内容审核） ===
        if (file != null && !file.isEmpty()) {
            try {
                Result uploadResult = uploadFileToCommonServer(
                        file,
                        FileUploadTypeEnum.NOVEL_CHAPTER.getCode(),
                        chapter.getNovelId(),
                        oldFileUrl
                );

                if (uploadResult.getCode() != 10000) {
                    return Result.error("更新章节文件失败");
                }

                String newContentUrl = (String) uploadResult.getData();

                Result contentResult = fileServiceFeign.getFileContent(newContentUrl);
                String content = (String) contentResult.getData();

                if (content != null && !content.isEmpty()) {
                    Map<String, String> request = new HashMap<>();
                    request.put("content", content);
                    AuditResultVO auditResultVO = OBJECT_MAPPER.convertValue(sensitiveWordServiceFeign.auditText(request).getData(), AuditResultVO.class);
                    if (!auditResultVO.getPassed()) {
                        return Result.buildResult(BizCodeEnum.SENSITIVE_WORD);
                    }

                    AiCommentAuditDTO aiAuditDTO = new AiCommentAuditDTO();
                    aiAuditDTO.setContent(content);
                    aiAuditDTO.setAimId(id);
                    aiAuditDTO.setAimType(AuditAimTypeEnum.CHAPTER.getValue());
                    aiAuditDTO.setLocalResult(auditResultVO);
                    Result aiAuditResult = aiAuditServiceFeign.auditWithAi(aiAuditDTO);
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

        // === 事务内：数据库操作 ===
        final boolean finalWordCountChanged = wordCountChanged;
        final Integer finalWordCount = wordCount;
        final Integer finalOldWordCount = oldWordCount;
        Integer result = transactionTemplate.execute(status -> {
            int rows = novelChapterMapper.updateSelective(chapter);
            if (rows == 1 && finalWordCountChanged && finalWordCount != null) {
                Novel novel = novelMapper.selectById(chapter.getNovelId());
                int delta = finalWordCount - finalOldWordCount;
                updateNovelAllWordCount(novel, delta);
            }
            return rows;
        });

        if (result != null && result == 1) {
            log.info("更新章节成功：章节ID={}", chapter.getId());
            cacheService.zAdd(CacheConstants.RANKING_NOVEL_LATEST, System.currentTimeMillis(), String.valueOf(chapter.getNovelId()));
            return Result.success(convertToVO(chapter));
        } else {
            log.error("更新章节失败：章节ID={}", chapter.getId());
            return Result.buildResult(BizCodeEnum.FAIL);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 通过 RestTemplate 上传文件到 common-server（替代 Feign，解决 multipart 兼容性问题）
     * 使用 @LoadBalanced RestTemplate，URL 中的 common-server 会被 Nacos 解析为实际地址
     */
    private Result uploadFileToCommonServer(MultipartFile file, Integer code, Long novelId, String oldFileUrl) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
            formData.add("file", new MultipartFileResource(file));
            formData.add("code", code.toString());
            if (novelId != null) {
                formData.add("novelId", novelId.toString());
            }
            if (oldFileUrl != null) {
                formData.add("oldFileUrl", oldFileUrl);
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(formData, headers);

            return restTemplate.postForObject(
                    "http://common-server/file/upload",
                    requestEntity,
                    Result.class
            );
        } catch (Exception e) {
            log.error("通过 RestTemplate 上传文件到 common-server 失败", e);
            return Result.error("上传文件失败：" + e.getMessage());
        }
    }

    /**
     * 自定义 Resource，将 MultipartFile 包装为可传输的资源并保留原始文件名
     * RestTemplate 的 FormHttpMessageConverter 通过 getFilename() 获取文件名
     */
    private static class MultipartFileResource extends ByteArrayResource {
        private final String filename;

        public MultipartFileResource(MultipartFile multipartFile) throws IOException {
            super(multipartFile.getBytes());
            this.filename = multipartFile.getOriginalFilename();
        }

        @Override
        public String getFilename() {
            return this.filename;
        }
    }

    private LoginUser getLoginUser() {
        return RoleContextUtil.getCurrentUser();
    }

    private Novel checkNovelOwnership(Long novelId, Long userId) {
        Novel novel = novelMapper.selectById(novelId);
        if (novel == null) {
            return null;
        }
        if (!Objects.equals(novel.getAuthorId(), userId)) {
            return null;
        }
        return novel;
    }

    private NovelChapter checkChapterOwnership(Long chapterId, Long userId) {
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

    private boolean isTitleExists(Long novelId, String title, Long excludeId) {
        LambdaQueryWrapper<NovelChapter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NovelChapter::getNovelId, novelId)
                .eq(NovelChapter::getTitle, title);
        if (excludeId != null) {
            wrapper.ne(NovelChapter::getId, excludeId);
        }
        return novelChapterMapper.selectCount(wrapper) > 0;
    }

    private int getNextChapterOrder(Long novelId) {
        LambdaQueryWrapper<NovelChapter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NovelChapter::getNovelId, novelId)
                .orderByDesc(NovelChapter::getChapterOrder)
                .last("LIMIT 1");
        NovelChapter lastChapter = novelChapterMapper.selectOne(wrapper);
        return (lastChapter == null) ? 1 : lastChapter.getChapterOrder() + 1;
    }

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
            updateNovelChapterCount(novel, 1);
            updateNovelAllWordCount(novel, wordCount != null ? wordCount : 0);
            log.info("保存章节成功：章节ID={}, 字数={}", chapter.getId(), wordCount);
            cacheService.zIncrBy(CacheConstants.RANKING_NOVEL_ONGOING, 1, String.valueOf(novel.getId()));
            cacheService.zAdd(CacheConstants.RANKING_NOVEL_LATEST, System.currentTimeMillis(), String.valueOf(novel.getId()));
            return Result.success(chapter);
        } else {
            log.error("保存章节记录失败");
            return Result.error("保存章节记录失败");
        }
    }

    private void updateNovelChapterCount(Novel novel, int delta) {
        int newCount = (novel.getChapterCount() == null ? 0 : novel.getChapterCount()) + delta;
        novel.setChapterCount(Math.max(0, newCount));
        novel.setUpdateTime(LocalDateTime.now());
        novelMapper.updateSelective(novel);
    }

    private void updateNovelAllWordCount(Novel novel, int delta) {
        int newWordCount = (novel.getAllWordCount() == null ? 0 : novel.getAllWordCount()) + delta;
        novel.setAllWordCount(Math.max(0, newWordCount));
        novel.setUpdateTime(LocalDateTime.now());
        novelMapper.updateSelective(novel);
        log.info("更新小说总字数：小说ID={}, 变化量={}, 新总字数={}", novel.getId(), delta, newWordCount);
    }

    private NovelChapterVO convertToVO(NovelChapter chapter) {
        NovelChapterVO vo = new NovelChapterVO();
        BeanUtils.copyProperties(chapter, vo);
        return vo;
    }
}
