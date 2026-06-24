package com.wang.visitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wang.common.constants.CacheConstants;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.feign.NovelServiceFeign;
import com.wang.common.result.Result;
import com.wang.common.service.CacheService;
import com.wang.pojo.entity.VisitorReadingProgress;
import com.wang.pojo.vo.VisitorReadingProgressVO;
import com.wang.visitor.mapper.VisitorReadingProgressMapper;
import com.wang.visitor.service.VisitorReadingProgressService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VisitorReadingProgressServiceImpl implements VisitorReadingProgressService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final VisitorReadingProgressMapper visitorReadingProgressMapper;
    private final NovelServiceFeign novelServiceFeign;
    private final CacheService cacheService;

    public VisitorReadingProgressServiceImpl(VisitorReadingProgressMapper visitorReadingProgressMapper,
                                              NovelServiceFeign novelServiceFeign,
                                              CacheService cacheService) {
        this.visitorReadingProgressMapper = visitorReadingProgressMapper;
        this.novelServiceFeign = novelServiceFeign;
        this.cacheService = cacheService;
    }

    @Override
    @Transactional
    public Result updateProgress(Long visitorId, Long novelId, Long chapterId, Integer chapterOrder) {
        LambdaQueryWrapper<VisitorReadingProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitorReadingProgress::getVisitorId, visitorId)
                .eq(VisitorReadingProgress::getNovelId, novelId);
        VisitorReadingProgress existing = visitorReadingProgressMapper.selectOne(queryWrapper);

        if (existing != null) {
            LambdaUpdateWrapper<VisitorReadingProgress> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(VisitorReadingProgress::getId, existing.getId())
                    .set(VisitorReadingProgress::getChapterId, chapterId)
                    .set(VisitorReadingProgress::getChapterOrder, chapterOrder)
                    .set(VisitorReadingProgress::getLastReadTime, LocalDateTime.now())
                    .set(VisitorReadingProgress::getUpdateTime, LocalDateTime.now());
            int result = visitorReadingProgressMapper.update(null, updateWrapper);
            if (result == 1) {
                clearProgressCache(visitorId, novelId);
                log.info("更新阅读进度成功：visitorId={}, novelId={}, chapterId={}", visitorId, novelId, chapterId);
                return Result.success("更新成功");
            }
            log.error("更新阅读进度失败：visitorId={}, novelId={}", visitorId, novelId);
            return Result.error("更新失败");
        }

        Result novelResult = novelServiceFeign.getNovelBasicInfo(novelId);
        if (novelResult.getCode() != BizCodeEnum.SUCCESS.getCode() || novelResult.getData() == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_NOT_FOUND);
        }

        java.util.Map<String, Object> novelInfo = OBJECT_MAPPER.convertValue(novelResult.getData(), new TypeReference<java.util.Map<String, Object>>() {});

        VisitorReadingProgress progress = new VisitorReadingProgress();
        progress.setVisitorId(visitorId);
        progress.setNovelId(novelId);
        progress.setNovelName((String) novelInfo.get("name"));
        progress.setNovelUrl((String) novelInfo.get("url"));
        progress.setAuthorName((String) novelInfo.get("authorName"));
        progress.setChapterId(chapterId);
        progress.setChapterOrder(chapterOrder);
        progress.setLastReadTime(LocalDateTime.now());
        progress.setCreateTime(LocalDateTime.now());
        progress.setUpdateTime(LocalDateTime.now());

        try {
            int result = visitorReadingProgressMapper.insert(progress);
            if (result == 1) {
                clearProgressCache(visitorId, novelId);
                cacheService.increment(CacheConstants.buildReadingCountKey(novelId));
                log.info("新增阅读进度成功：visitorId={}, novelId={}, chapterId={}", visitorId, novelId, chapterId);
                return Result.success("记录成功");
            }
            log.error("新增阅读进度失败：visitorId={}, novelId={}", visitorId, novelId);
            return Result.error("记录失败");
        } catch (DuplicateKeyException e) {
            log.info("并发插入冲突，转为更新：visitorId={}, novelId={}", visitorId, novelId);
            LambdaUpdateWrapper<VisitorReadingProgress> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(VisitorReadingProgress::getVisitorId, visitorId)
                    .eq(VisitorReadingProgress::getNovelId, novelId)
                    .set(VisitorReadingProgress::getChapterId, chapterId)
                    .set(VisitorReadingProgress::getChapterOrder, chapterOrder)
                    .set(VisitorReadingProgress::getLastReadTime, LocalDateTime.now())
                    .set(VisitorReadingProgress::getUpdateTime, LocalDateTime.now());
            visitorReadingProgressMapper.update(null, updateWrapper);
            clearProgressCache(visitorId, novelId);
            return Result.success("记录成功");
        }
    }

    @Override
    public Result getProgress(Long visitorId, Long novelId) {
        String cacheKey = CacheConstants.buildReadingProgressKey(visitorId, novelId);
        VisitorReadingProgressVO cachedVO = cacheService.get(cacheKey, VisitorReadingProgressVO.class);
        if (cachedVO != null) {
            log.info("从缓存获取阅读进度：visitorId={}, novelId={}", visitorId, novelId);
            return Result.success(cachedVO);
        }

        LambdaQueryWrapper<VisitorReadingProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitorReadingProgress::getVisitorId, visitorId)
                .eq(VisitorReadingProgress::getNovelId, novelId);
        VisitorReadingProgress progress = visitorReadingProgressMapper.selectOne(queryWrapper);

        if (progress == null) {
            return Result.success(null);
        }

        VisitorReadingProgressVO vo = convertToVO(progress);
        cacheService.set(cacheKey, vo, CacheConstants.READING_PROGRESS_TTL);
        return Result.success(vo);
    }

    @Override
    public Result getRecentList(Long visitorId) {
        LambdaQueryWrapper<VisitorReadingProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitorReadingProgress::getVisitorId, visitorId)
                .orderByDesc(VisitorReadingProgress::getLastReadTime);

        List<VisitorReadingProgress> progressList = visitorReadingProgressMapper.selectList(queryWrapper);
        List<VisitorReadingProgressVO> voList = progressList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        log.info("获取最近阅读列表成功：visitorId={}, 数量={}", visitorId, voList.size());
        return Result.success(voList);
    }

    @Override
    @Transactional
    public Result deleteProgress(Long visitorId, Long novelId) {
        LambdaQueryWrapper<VisitorReadingProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitorReadingProgress::getVisitorId, visitorId)
                .eq(VisitorReadingProgress::getNovelId, novelId);

        int result = visitorReadingProgressMapper.delete(queryWrapper);
        if (result == 1) {
            clearProgressCache(visitorId, novelId);
            cacheService.decrement(CacheConstants.buildReadingCountKey(novelId));
            log.info("删除阅读进度成功：visitorId={}, novelId={}", visitorId, novelId);
            return Result.success("删除成功");
        }
        log.warn("删除阅读进度失败，记录不存在：visitorId={}, novelId={}", visitorId, novelId);
        return Result.error("记录不存在");
    }

    private void clearProgressCache(Long visitorId, Long novelId) {
        cacheService.delete(CacheConstants.buildReadingProgressKey(visitorId, novelId));
    }

    @Override
    public Result getReadingCount(Long novelId) {
        String countKey = CacheConstants.buildReadingCountKey(novelId);
        Long count = cacheService.get(countKey, Long.class);
        if (count != null && count >= 0) {
            return Result.success(count);
        }
        Long dbCount = visitorReadingProgressMapper.countByNovelId(novelId);
        cacheService.set(countKey, dbCount, CacheConstants.COUNTER_TTL);
        return Result.success(dbCount);
    }

    private VisitorReadingProgressVO convertToVO(VisitorReadingProgress progress) {
        VisitorReadingProgressVO vo = new VisitorReadingProgressVO();
        BeanUtils.copyProperties(progress, vo);
        return vo;
    }
}
