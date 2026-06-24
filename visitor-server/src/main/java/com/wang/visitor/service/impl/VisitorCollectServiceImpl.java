package com.wang.visitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.feign.NovelServiceFeign;
import com.wang.common.result.Result;
import com.wang.common.service.CacheService;
import com.wang.common.constants.CacheConstants;
import com.wang.visitor.mapper.VisitorCollectMapper;
import com.wang.visitor.service.VisitorCollectService;
import com.wang.pojo.entity.VisitorCollect;
import com.wang.pojo.vo.VisitorCollectVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 小说收藏服务实现类
 */
@Slf4j
@Service
public class VisitorCollectServiceImpl implements VisitorCollectService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final VisitorCollectMapper visitorCollectMapper;
    private final NovelServiceFeign novelServiceFeign;
    private final CacheService cacheService;

    public VisitorCollectServiceImpl(VisitorCollectMapper visitorCollectMapper,
                                      NovelServiceFeign novelServiceFeign,
                                      CacheService cacheService) {
        this.visitorCollectMapper = visitorCollectMapper;
        this.novelServiceFeign = novelServiceFeign;
        this.cacheService = cacheService;
    }

    @Override
    @Transactional
    public Result addCollect(Long visitorId, Long novelId) {
        // 检查是否已收藏
        LambdaQueryWrapper<VisitorCollect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitorCollect::getVisitorId, visitorId)
                .eq(VisitorCollect::getNovelId, novelId);
        if (visitorCollectMapper.selectCount(queryWrapper) > 0) {
            return Result.error("已收藏该小说");
        }

        // 通过Feign调用novel-server获取小说信息
        Result novelResult = novelServiceFeign.getNovelBasicInfo(novelId);
        if (novelResult.getCode() != BizCodeEnum.SUCCESS.getCode() || novelResult.getData() == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_NOT_FOUND);
        }

        java.util.Map<String, Object> novelInfo = OBJECT_MAPPER.convertValue(novelResult.getData(), new TypeReference<java.util.Map<String, Object>>() {});

        // 创建收藏记录
        VisitorCollect collect = new VisitorCollect();
        collect.setVisitorId(visitorId);
        collect.setNovelId(novelId);
        collect.setNovelName((String) novelInfo.get("name"));
        collect.setNovelUrl((String) novelInfo.get("url"));
        collect.setAuthorName((String) novelInfo.get("authorName"));
        collect.setAuthorAvatar((String) novelInfo.get("authorAvatar"));
        Object authorRankObj = novelInfo.get("authorRank");
        collect.setAuthorRank(authorRankObj != null ? ((Number) authorRankObj).intValue() : 1);
        collect.setCreateTime(LocalDateTime.now());

        int result = visitorCollectMapper.insert(collect);
        if (result == 1) {
            String checkKey = CacheConstants.buildCollectCheckKey(visitorId, novelId);
            cacheService.delete(checkKey);
            cacheService.increment(CacheConstants.buildCollectCountKey(visitorId));
            cacheService.increment(CacheConstants.buildNovelCollectCountKey(novelId));

            log.info("添加收藏成功：用户ID={}, 小说ID={}", visitorId, novelId);
            return Result.success("收藏成功");
        } else {
            log.error("添加收藏失败：用户ID={}, 小说ID={}", visitorId, novelId);
            return Result.error("收藏失败");
        }
    }

    @Override
    @Transactional
    public Result removeCollect(Long visitorId, Long novelId) {
        LambdaQueryWrapper<VisitorCollect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitorCollect::getVisitorId, visitorId)
                .eq(VisitorCollect::getNovelId, novelId);

        int result = visitorCollectMapper.delete(queryWrapper);
        if (result == 1) {
            String checkKey = CacheConstants.buildCollectCheckKey(visitorId, novelId);
            cacheService.delete(checkKey);
            cacheService.decrement(CacheConstants.buildCollectCountKey(visitorId));
            cacheService.decrement(CacheConstants.buildNovelCollectCountKey(novelId));

            log.info("取消收藏成功：用户ID={}, 小说ID={}", visitorId, novelId);
            cacheService.zIncrBy(CacheConstants.RANKING_NOVEL_COLLECT, -1, String.valueOf(novelId));
            return Result.success("取消收藏成功");
        } else {
            log.warn("取消收藏失败，记录不存在：用户ID={}, 小说ID={}", visitorId, novelId);
            return Result.error("未收藏该小说");
        }
    }

    @Override
    public Result getCollectList(Long visitorId) {
        LambdaQueryWrapper<VisitorCollect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitorCollect::getVisitorId, visitorId)
                .orderByDesc(VisitorCollect::getCreateTime);

        List<VisitorCollect> collectList = visitorCollectMapper.selectList(queryWrapper);
        List<VisitorCollectVO> voList = collectList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        log.info("获取收藏列表成功：用户ID={}, 数量={}", visitorId, voList.size());
        return Result.success(voList);
    }

    @Override
    public Result checkCollect(Long visitorId, Long novelId) {
        // 先从缓存获取
        String cacheKey = CacheConstants.buildCollectCheckKey(visitorId, novelId);
        Boolean cachedResult = cacheService.get(cacheKey, Boolean.class);
        if (cachedResult != null) {
            log.info("从缓存获取收藏检查结果");
            return Result.success(cachedResult);
        }
        
        // 缓存未命中，查询数据库
        LambdaQueryWrapper<VisitorCollect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitorCollect::getVisitorId, visitorId)
                .eq(VisitorCollect::getNovelId, novelId);

        boolean isCollected = visitorCollectMapper.selectCount(queryWrapper) > 0;
        
        // 存入缓存
        cacheService.set(cacheKey, isCollected, CacheConstants.CHECK_TTL);
        log.info("收藏检查结果已缓存");
        
        return Result.success(isCollected);
    }

    @Override
    public Result getCollectCount(Long visitorId) {
        String countKey = CacheConstants.buildCollectCountKey(visitorId);
        Long count = cacheService.get(countKey, Long.class);
        if (count != null && count >= 0) {
            return Result.success(count);
        }
        Long dbCount = visitorCollectMapper.countByVisitorId(visitorId);
        cacheService.set(countKey, dbCount, CacheConstants.COUNTER_TTL);
        return Result.success(dbCount);
    }

    /**
     * 转换为VO
     */
    private VisitorCollectVO convertToVO(VisitorCollect collect) {
        VisitorCollectVO vo = new VisitorCollectVO();
        BeanUtils.copyProperties(collect, vo);
        return vo;
    }
}
