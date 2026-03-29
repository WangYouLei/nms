package com.wang.visitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.result.Result;
import com.wang.common.service.CacheService;
import com.wang.common.constants.CacheConstants;
import com.wang.visitor.mapper.VisitorCollectMapper;
import com.wang.visitor.mapper.NovelMapper;
import com.wang.visitor.service.VisitorCollectService;
import com.wang.pojo.entity.Novel;
import com.wang.pojo.entity.VisitorCollect;
import com.wang.pojo.vo.VisitorCollectVO;
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

    private final VisitorCollectMapper visitorCollectMapper;
    private final NovelMapper novelMapper;
    private final CacheService cacheService;

    public VisitorCollectServiceImpl(VisitorCollectMapper visitorCollectMapper,
                                      NovelMapper novelMapper,
                                      CacheService cacheService) {
        this.visitorCollectMapper = visitorCollectMapper;
        this.novelMapper = novelMapper;
        this.cacheService = cacheService;
    }

    @Override
    @Transactional
    public Result addCollect(Integer visitorId, Integer novelId) {
        // 检查是否已收藏
        LambdaQueryWrapper<VisitorCollect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitorCollect::getVisitorId, visitorId)
                .eq(VisitorCollect::getNovelId, novelId);
        if (visitorCollectMapper.selectCount(queryWrapper) > 0) {
            return Result.error("已收藏该小说");
        }

        // 查询小说信息
        Novel novel = novelMapper.selectById(novelId);
        if (novel == null || novel.getIsDel()) {
            return Result.buildResult(BizCodeEnum.NOVEL_NOT_FOUND);
        }

        // 创建收藏记录
        VisitorCollect collect = new VisitorCollect();
        collect.setVisitorId(visitorId);
        collect.setNovelId(novelId);
        collect.setNovelName(novel.getName());
        collect.setNovelUrl(novel.getUrl());
        collect.setAuthorName(novel.getAuthorName());
        collect.setAuthorAvatar(novel.getAuthorAvatar());
        collect.setAuthorRank(novel.getAuthorRank());
        collect.setCreateTime(LocalDateTime.now());

        int result = visitorCollectMapper.insert(collect);
        if (result == 1) {
            // 删除相关缓存
            String checkKey = CacheConstants.buildCollectCheckKey(visitorId, novelId);
            String countKey = CacheConstants.buildCollectCountKey(visitorId);
            cacheService.delete(checkKey);
            cacheService.delete(countKey);
            
            log.info("添加收藏成功：用户ID={}, 小说ID={}", visitorId, novelId);
            return Result.success("收藏成功");
        } else {
            log.error("添加收藏失败：用户ID={}, 小说ID={}", visitorId, novelId);
            return Result.error("收藏失败");
        }
    }

    @Override
    @Transactional
    public Result removeCollect(Integer visitorId, Integer novelId) {
        LambdaQueryWrapper<VisitorCollect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitorCollect::getVisitorId, visitorId)
                .eq(VisitorCollect::getNovelId, novelId);

        int result = visitorCollectMapper.delete(queryWrapper);
        if (result == 1) {
            // 删除相关缓存
            String checkKey = CacheConstants.buildCollectCheckKey(visitorId, novelId);
            String countKey = CacheConstants.buildCollectCountKey(visitorId);
            cacheService.delete(checkKey);
            cacheService.delete(countKey);
            
            log.info("取消收藏成功：用户ID={}, 小说ID={}", visitorId, novelId);
            return Result.success("取消收藏成功");
        } else {
            log.warn("取消收藏失败，记录不存在：用户ID={}, 小说ID={}", visitorId, novelId);
            return Result.error("未收藏该小说");
        }
    }

    @Override
    public Result getCollectList(Integer visitorId) {
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
    public Result checkCollect(Integer visitorId, Integer novelId) {
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
    public Result getCollectCount(Integer visitorId) {
        // 先从缓存获取
        String cacheKey = CacheConstants.buildCollectCountKey(visitorId);
        Integer cachedCount = cacheService.get(cacheKey, Integer.class);
        if (cachedCount != null) {
            log.info("从缓存获取收藏数量");
            return Result.success(cachedCount);
        }
        
        // 缓存未命中，查询数据库
        Integer count = visitorCollectMapper.countByVisitorId(visitorId);
        
        // 存入缓存
        cacheService.set(cacheKey, count, CacheConstants.COUNT_TTL);
        log.info("收藏数量已缓存");
        
        return Result.success(count);
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
