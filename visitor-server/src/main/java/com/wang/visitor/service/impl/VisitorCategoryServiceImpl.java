package com.wang.visitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wang.common.result.Result;
import com.wang.pojo.entity.NovelCategory;
import com.wang.pojo.vo.NovelCategoryVO;
import com.wang.visitor.mapper.VisitorCategoryMapper;
import com.wang.visitor.service.VisitorCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 访客端分类服务实现类
 */
@Slf4j
@Service
public class VisitorCategoryServiceImpl implements VisitorCategoryService {

    private final VisitorCategoryMapper visitorCategoryMapper;

    public VisitorCategoryServiceImpl(VisitorCategoryMapper visitorCategoryMapper) {
        this.visitorCategoryMapper = visitorCategoryMapper;
    }

    @Override
    public Result getAllCategories() {
        log.info("获取所有分类");
        List<NovelCategory> list = visitorCategoryMapper.selectList(null);
        List<NovelCategoryVO> voList = list.stream().map(this::convertToVO).collect(Collectors.toList());
        return Result.success(voList);
    }

    @Override
    public Result getCategoriesByChannel(Integer category) {
        log.info("根据频道获取分类：category={}", category);
        LambdaQueryWrapper<NovelCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelCategory::getCategory, category);
        List<NovelCategory> list = visitorCategoryMapper.selectList(queryWrapper);
        List<NovelCategoryVO> voList = list.stream().map(this::convertToVO).collect(Collectors.toList());
        return Result.success(voList);
    }

    @Override
    public Result getHotCategories() {
        log.info("获取热门分类");
        LambdaQueryWrapper<NovelCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelCategory::getIsHot, 1);
        List<NovelCategory> list = visitorCategoryMapper.selectList(queryWrapper);
        List<NovelCategoryVO> voList = list.stream().map(this::convertToVO).collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 转换为VO
     */
    private NovelCategoryVO convertToVO(NovelCategory entity) {
        NovelCategoryVO vo = new NovelCategoryVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setCategoryName(entity.getCategory() == 1 ? "男频" : "女频");
        return vo;
    }
}