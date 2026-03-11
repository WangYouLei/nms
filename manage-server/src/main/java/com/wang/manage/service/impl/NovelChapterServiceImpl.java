package com.wang.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.interceptor.LoginInterceptor;
import com.wang.common.model.LoginUser;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.manage.config.MinioInfo;
import com.wang.manage.mapper.NovelChapterMapper;
import com.wang.manage.mapper.NovelMapper;
import com.wang.manage.service.NovelChapterService;
import com.wang.pojo.dto.NovelChapterDTO;
import com.wang.pojo.entity.Novel;
import com.wang.pojo.entity.NovelChapter;
import com.wang.pojo.vo.NovelChapterVO;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final MinioClient minioClient;
    private final MinioInfo minioInfo;

    @Autowired
    public NovelChapterServiceImpl(NovelChapterMapper novelChapterMapper,
                                    NovelMapper novelMapper,
                                    MinioClient minioClient,
                                    MinioInfo minioInfo) {
        this.novelChapterMapper = novelChapterMapper;
        this.novelMapper = novelMapper;
        this.minioClient = minioClient;
        this.minioInfo = minioInfo;
    }

    /**
     * 上传章节
     */
    @Override
    @Transactional
    public Result uploadChapter(Integer novelId, String title, MultipartFile file) {
        // 获取当前登录用户
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        log.info("上传章节：小说ID={}, 章节标题={}, 用户ID={}", novelId, title, loginUser.getId());

        // 检查小说是否存在且属于当前用户
        Novel novel = novelMapper.selectById(novelId);
        if (novel == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_NOT_FOUND);
        }
        if (!Objects.equals(novel.getAuthorId(), loginUser.getId())) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        // 检查章节标题是否已存在
        LambdaQueryWrapper<NovelChapter> titleWrapper = new LambdaQueryWrapper<>();
        titleWrapper.eq(NovelChapter::getNovelId, novelId)
                .eq(NovelChapter::getTitle, title);
        if (novelChapterMapper.selectCount(titleWrapper) > 0) {
            return Result.error("章节标题已存在");
        }

        // 上传文件到MinIO
        // 存储路径: manager/NOVEL_CHAPTER/{小说ID}/{章节标题}.md
        String fileName = sanitizeFileName(title) + ".md";
        String objectName = "manager/NOVEL_CHAPTER/" + novelId + "/" + fileName;

        try {
            ObjectWriteResponse response = minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioInfo.getBucketName())
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType("text/markdown")
                            .build()
            );

            if (response == null) {
                log.error("上传章节文件失败");
                return Result.error("上传章节文件失败");
            }

            // 获取当前小说的最大章节顺序
            LambdaQueryWrapper<NovelChapter> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(NovelChapter::getNovelId, novelId)
                    .orderByDesc(NovelChapter::getChapterOrder)
                    .last("LIMIT 1");
            NovelChapter lastChapter = novelChapterMapper.selectOne(orderWrapper);
            int nextOrder = (lastChapter == null) ? 1 : lastChapter.getChapterOrder() + 1;

            // 构建完整的URL
            String contentUrl = minioInfo.getEndpoint() + "/" + minioInfo.getBucketName() + "/" + objectName;

            // 保存章节记录到数据库
            NovelChapter chapter = new NovelChapter();
            chapter.setNovelId(novelId);
            chapter.setTitle(title);
            chapter.setContentUrl(contentUrl);
            chapter.setChapterOrder(nextOrder);
            chapter.setCreateTime(LocalDateTime.now());
            chapter.setUpdateTime(LocalDateTime.now());

            int result = novelChapterMapper.insert(chapter);
            if (result == 1) {
                // 更新小说的章节计数
                novel.setChapterCount(novel.getChapterCount() == null ? 1 : novel.getChapterCount() + 1);
                novel.setUpdateTime(LocalDateTime.now());
                novelMapper.updateById(novel);

                log.info("上传章节成功：章节ID={}", chapter.getId());
                return Result.success(chapter);
            } else {
                log.error("保存章节记录失败");
                return Result.error("保存章节记录失败");
            }

        } catch (Exception e) {
            log.error("上传章节异常: {}", e.getMessage());
            return Result.error("上传章节失败：" + e.getMessage());
        }
    }

    /**
     * 删除章节
     */
    @Override
    @Transactional
    public Result deleteChapter(Integer id) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        log.info("删除章节：章节ID={}, 用户ID={}", id, loginUser.getId());

        // 查询章节
        NovelChapter chapter = novelChapterMapper.selectById(id);
        if (chapter == null) {
            return Result.error("章节不存在");
        }

        // 检查小说归属
        Novel novel = novelMapper.selectById(chapter.getNovelId());
        if (novel == null || !Objects.equals(novel.getAuthorId(), loginUser.getId())) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        // 删除章节记录
        int result = novelChapterMapper.deleteById(id);
        if (result == 1) {
            // 更新小说的章节计数
            novel.setChapterCount(novel.getChapterCount() == null ? 0 : Math.max(0, novel.getChapterCount() - 1));
            novel.setUpdateTime(LocalDateTime.now());
            novelMapper.updateById(novel);

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
    public Result updateChapter(NovelChapterDTO chapterDTO) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        log.info("更新章节：章节ID={}", chapterDTO.getId());

        // 查询章节
        NovelChapter chapter = novelChapterMapper.selectById(chapterDTO.getId());
        if (chapter == null) {
            return Result.error("章节不存在");
        }

        // 检查小说归属
        Novel novel = novelMapper.selectById(chapter.getNovelId());
        if (novel == null || !Objects.equals(novel.getAuthorId(), loginUser.getId())) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        // 更新章节信息
        if (chapterDTO.getTitle() != null) {
            // 检查新标题是否已存在
            LambdaQueryWrapper<NovelChapter> titleWrapper = new LambdaQueryWrapper<>();
            titleWrapper.eq(NovelChapter::getNovelId, chapter.getNovelId())
                    .eq(NovelChapter::getTitle, chapterDTO.getTitle())
                    .ne(NovelChapter::getId, chapterDTO.getId());
            if (novelChapterMapper.selectCount(titleWrapper) > 0) {
                return Result.error("章节标题已存在");
            }
            chapter.setTitle(chapterDTO.getTitle());
        }

        if (chapterDTO.getChapterOrder() != null) {
            chapter.setChapterOrder(chapterDTO.getChapterOrder());
        }

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
     * 分页查询章节列表
     */
    @Override
    public Result getChapterList(Integer novelId, Integer pageNum, Integer pageSize) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        log.info("查询章节列表：小说ID={}, 用户ID={}", novelId, loginUser.getId());

        // 检查小说归属
        Novel novel = novelMapper.selectById(novelId);
        if (novel == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_NOT_FOUND);
        }
        if (!Objects.equals(novel.getAuthorId(), loginUser.getId())) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        // 参数校验
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        // 分页查询
        Page<NovelChapter> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<NovelChapter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelChapter::getNovelId, novelId)
                .orderByAsc(NovelChapter::getChapterOrder);

        Page<NovelChapter> result = novelChapterMapper.selectPage(page, queryWrapper);

        // 转换为VO
        List<NovelChapterVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageResult<NovelChapterVO> pageResult = PageResult.build(
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getTotal(),
                voList
        );

        return Result.success(pageResult);
    }

    /**
     * 获取章节详情
     */
    @Override
    public Result getChapterDetail(Integer id) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        log.info("获取章节详情：章节ID={}", id);

        NovelChapter chapter = novelChapterMapper.selectById(id);
        if (chapter == null) {
            return Result.error("章节不存在");
        }

        // 检查小说归属
        Novel novel = novelMapper.selectById(chapter.getNovelId());
        if (novel == null || !Objects.equals(novel.getAuthorId(), loginUser.getId())) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        return Result.success(convertToVO(chapter));
    }

    /**
     * 转换为VO
     */
    private NovelChapterVO convertToVO(NovelChapter chapter) {
        NovelChapterVO vo = new NovelChapterVO();
        BeanUtils.copyProperties(chapter, vo);
        return vo;
    }

    /**
     * 清理文件名，移除不安全字符
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "untitled";
        }
        // 移除Windows和Linux不允许的字符
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_")
                .trim();
    }
}