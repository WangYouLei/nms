package com.wang.visitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.result.Result;
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

    public VisitorCollectServiceImpl(VisitorCollectMapper visitorCollectMapper,
                                      NovelMapper novelMapper) {
        this.visitorCollectMapper = visitorCollectMapper;
        this.novelMapper = novelMapper;
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
        LambdaQueryWrapper<VisitorCollect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitorCollect::getVisitorId, visitorId)
                .eq(VisitorCollect::getNovelId, novelId);

        boolean isCollected = visitorCollectMapper.selectCount(queryWrapper) > 0;
        return Result.success(isCollected);
    }

    @Override
    public Result getCollectCount(Integer visitorId) {
        Integer count = visitorCollectMapper.countByVisitorId(visitorId);
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