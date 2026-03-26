package com.wang.visitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.pojo.dto.VisitorFollowDTO;
import com.wang.pojo.entity.VisitorFollow;
import com.wang.pojo.vo.VisitorFollowVO;
import com.wang.visitor.mapper.VisitorFollowMapper;
import com.wang.visitor.service.VisitorFollowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 访客关注作者服务实现类
 */
@Slf4j
@Service
public class VisitorFollowServiceImpl implements VisitorFollowService {

    private final VisitorFollowMapper visitorFollowMapper;

    public VisitorFollowServiceImpl(VisitorFollowMapper visitorFollowMapper) {
        this.visitorFollowMapper = visitorFollowMapper;
    }

    @Override
    @Transactional
    public Result follow(VisitorFollowDTO dto) {
        log.info("访客关注作者：visitorId={}, authorId={}", dto.getVisitorId(), dto.getAuthorId());

        // 检查是否已关注
        if (visitorFollowMapper.existsByVisitorIdAndAuthorId(dto.getVisitorId(), dto.getAuthorId())) {
            log.warn("已关注该作者：visitorId={}, authorId={}", dto.getVisitorId(), dto.getAuthorId());
            return Result.error("已关注该作者");
        }

        // 创建关注记录，使用前端传入的作者信息
        VisitorFollow follow = new VisitorFollow();
        follow.setVisitorId(dto.getVisitorId());
        follow.setAuthorId(dto.getAuthorId());
        follow.setAuthorName(dto.getAuthorName());
        follow.setAuthorAvatar(dto.getAuthorAvatar());
        follow.setAuthorRank(dto.getAuthorRank());
        follow.setCreateTime(LocalDateTime.now());
        follow.setUpdateTime(LocalDateTime.now());

        int result = visitorFollowMapper.insert(follow);
        if (result == 1) {
            log.info("关注成功：visitorId={}, authorId={}", dto.getVisitorId(), dto.getAuthorId());
            return Result.success("关注成功");
        } else {
            log.error("关注失败：visitorId={}, authorId={}", dto.getVisitorId(), dto.getAuthorId());
            return Result.error("关注失败");
        }
    }

    @Override
    @Transactional
    public Result unfollow(Long visitorId, Long authorId) {
        log.info("取消关注：visitorId={}, authorId={}", visitorId, authorId);

        LambdaQueryWrapper<VisitorFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitorFollow::getVisitorId, visitorId)
                .eq(VisitorFollow::getAuthorId, authorId);

        int result = visitorFollowMapper.delete(queryWrapper);
        if (result == 1) {
            log.info("取消关注成功：visitorId={}, authorId={}", visitorId, authorId);
            return Result.success("取消关注成功");
        } else {
            log.warn("未关注该作者：visitorId={}, authorId={}", visitorId, authorId);
            return Result.error("未关注该作者");
        }
    }

    @Override
    public Result checkFollow(Long visitorId, Long authorId) {
        log.info("检查是否关注：visitorId={}, authorId={}", visitorId, authorId);
        boolean exists = visitorFollowMapper.existsByVisitorIdAndAuthorId(visitorId, authorId);
        return Result.success(exists);
    }

    @Override
    public Result getMyFollows(Long visitorId, Integer pageNum, Integer pageSize) {
        log.info("获取我的关注列表：visitorId={}", visitorId);

        LambdaQueryWrapper<VisitorFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitorFollow::getVisitorId, visitorId)
                .orderByDesc(VisitorFollow::getCreateTime);

        Page<VisitorFollow> page = new Page<>(pageNum, pageSize);
        Page<VisitorFollow> resultPage = visitorFollowMapper.selectPage(page, queryWrapper);

        List<VisitorFollowVO> voList = new ArrayList<>();
        for (VisitorFollow follow : resultPage.getRecords()) {
            VisitorFollowVO vo = convertToVO(follow);
            voList.add(vo);
        }

        PageResult<VisitorFollowVO> pageResult = PageResult.build(
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize(),
                resultPage.getTotal(),
                voList
        );

        return Result.success(pageResult);
    }

    @Override
    public Result getFollowers(Long authorId, Integer pageNum, Integer pageSize) {
        log.info("获取作者粉丝列表：authorId={}", authorId);

        LambdaQueryWrapper<VisitorFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitorFollow::getAuthorId, authorId)
                .orderByDesc(VisitorFollow::getCreateTime);

        Page<VisitorFollow> page = new Page<>(pageNum, pageSize);
        Page<VisitorFollow> resultPage = visitorFollowMapper.selectPage(page, queryWrapper);

        List<VisitorFollowVO> voList = new ArrayList<>();
        for (VisitorFollow follow : resultPage.getRecords()) {
            VisitorFollowVO vo = convertToVO(follow);
            voList.add(vo);
        }

        PageResult<VisitorFollowVO> pageResult = PageResult.build(
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize(),
                resultPage.getTotal(),
                voList
        );

        return Result.success(pageResult);
    }

    @Override
    public Result getMyFollowCount(Long visitorId) {
        log.info("获取我的关注数量：visitorId={}", visitorId);
        int count = visitorFollowMapper.countByVisitorId(visitorId);
        return Result.success(count);
    }

    @Override
    public Result getFollowerCount(Long authorId) {
        log.info("获取作者粉丝数量：authorId={}", authorId);
        int count = visitorFollowMapper.countByAuthorId(authorId);
        return Result.success(count);
    }

    /**
     * 转换为VO
     */
    private VisitorFollowVO convertToVO(VisitorFollow follow) {
        VisitorFollowVO vo = new VisitorFollowVO();
        BeanUtils.copyProperties(follow, vo);
        vo.setAuthorRankName(getAuthorRankName(follow.getAuthorRank()));
        return vo;
    }

    /**
     * 获取作者等级名称
     */
    private String getAuthorRankName(Integer rank) {
        if (rank == null) {
            return "未知";
        }
        return switch (rank) {
            case 1 -> "执笔者";
            case 2 -> "织梦师";
            case 3 -> "造界者";
            case 4 -> "渡舟人";
            case 5 -> "燃灯者";
            default -> "未知";
        };
    }
}