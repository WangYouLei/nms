package com.wang.novel.service.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.novel.mapper.NovelCategoryMapper;
import com.wang.novel.mapper.NovelCategoryRelationMapper;
import com.wang.novel.mapper.NovelMapper;
import com.wang.novel.service.manager.NovelCategoryService;
import com.wang.pojo.dto.NovelCategoryDTO;
import com.wang.pojo.dto.NovelCategoryRelationDTO;
import com.wang.pojo.entity.NovelCategory;
import com.wang.pojo.entity.NovelCategoryRelation;
import com.wang.pojo.vo.NovelCategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 小说分类服务实现类
 */
@Slf4j
@Service
public class NovelCategoryServiceImpl implements NovelCategoryService {

    private final NovelCategoryMapper categoryMapper;
    private final NovelCategoryRelationMapper relationMapper;
    private final NovelMapper novelMapper;

    public NovelCategoryServiceImpl(NovelCategoryMapper categoryMapper, 
                                    NovelCategoryRelationMapper relationMapper,
                                    NovelMapper novelMapper) {
        this.categoryMapper = categoryMapper;
        this.relationMapper = relationMapper;
        this.novelMapper = novelMapper;
    }

    @Override
    public Result addCategory(NovelCategoryDTO dto) {
        log.info("添加分类：type={}, category={}", dto.getType(), dto.getCategory());

        // 检查分类是否已存在（同一type和category组合）
        LambdaQueryWrapper<NovelCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelCategory::getType, dto.getType())
                .eq(NovelCategory::getCategory, dto.getCategory());
        if (categoryMapper.selectCount(queryWrapper) > 0) {
            return Result.buildResult(BizCodeEnum.NOVEL_CATEGORY_EXIST);
        }

        NovelCategory entity = new NovelCategory();
        BeanUtils.copyProperties(dto, entity);
        entity.setIsHot(dto.getIsHot() != null ? dto.getIsHot() : 0);
        categoryMapper.insert(entity);
        return Result.success(convertToVO(entity));
    }

    @Override
    public Result updateCategory(NovelCategoryDTO dto) {
        log.info("修改分类：ID={}", dto.getId());

        NovelCategory entity = categoryMapper.selectById(dto.getId());
        if (entity == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_CATEGORY_NOT_FOUND);
        }

        // 检查分类是否重复
        if (!entity.getType().equals(dto.getType()) || !entity.getCategory().equals(dto.getCategory())) {
            LambdaQueryWrapper<NovelCategory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(NovelCategory::getType, dto.getType())
                    .eq(NovelCategory::getCategory, dto.getCategory())
                    .ne(NovelCategory::getId, dto.getId());
            if (categoryMapper.selectCount(queryWrapper) > 0) {
                return Result.buildResult(BizCodeEnum.NOVEL_CATEGORY_EXIST);
            }
        }

        BeanUtils.copyProperties(dto, entity);
        categoryMapper.updateById(entity);
        return Result.success(convertToVO(entity));
    }

    @Override
    @Transactional
    public Result deleteCategory(Integer id) {
        log.info("删除分类：ID={}", id);

        NovelCategory entity = categoryMapper.selectById(id);
        if (entity == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_CATEGORY_NOT_FOUND);
        }

        // 检查是否有小说使用该分类
        LambdaQueryWrapper<NovelCategoryRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelCategoryRelation::getCategoryId, id);
        if (relationMapper.selectCount(queryWrapper) > 0) {
            return Result.error("该分类下存在小说，无法删除");
        }

        categoryMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @Override
    public Result getCategoryById(Integer id) {
        NovelCategory entity = categoryMapper.selectById(id);
        if (entity == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_CATEGORY_NOT_FOUND);
        }
        return Result.success(convertToVO(entity));
    }

    @Override
    public Result getAllCategories() {
        List<NovelCategory> list = categoryMapper.selectList(null);
        List<NovelCategoryVO> voList = list.stream().map(this::convertToVO).collect(Collectors.toList());
        return Result.success(voList);
    }

    @Override
    public Result getCategoriesByCategory(Integer category) {
        LambdaQueryWrapper<NovelCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelCategory::getCategory, category);
        List<NovelCategory> list = categoryMapper.selectList(queryWrapper);
        List<NovelCategoryVO> voList = list.stream().map(this::convertToVO).collect(Collectors.toList());
        return Result.success(voList);
    }

    @Override
    public Result getHotCategories() {
        LambdaQueryWrapper<NovelCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelCategory::getIsHot, 1);
        List<NovelCategory> list = categoryMapper.selectList(queryWrapper);
        List<NovelCategoryVO> voList = list.stream().map(this::convertToVO).collect(Collectors.toList());
        return Result.success(voList);
    }

    @Override
    public Result getCategoryList(Integer pageNum, Integer pageSize, String type, Integer category) {
        Page<NovelCategory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<NovelCategory> queryWrapper = new LambdaQueryWrapper<>();
        
        if (type != null && !type.isEmpty()) {
            queryWrapper.like(NovelCategory::getType, type);
        }
        if (category != null) {
            queryWrapper.eq(NovelCategory::getCategory, category);
        }
        queryWrapper.orderByDesc(NovelCategory::getCreateTime);

        Page<NovelCategory> result = categoryMapper.selectPage(page, queryWrapper);
        List<NovelCategoryVO> voList = result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());

        PageResult<NovelCategoryVO> pageResult = new PageResult<>();
        pageResult.setTotal(result.getTotal());
        pageResult.setList(voList);
        return Result.success(pageResult);
    }

    @Override
    @Transactional
    public Result setNovelCategory(NovelCategoryRelationDTO dto) {
        log.info("设置小说分类：小说ID={}, 分类ID={}", dto.getNovelId(), dto.getCategoryId());

        // 检查小说是否存在
        if (novelMapper.selectById(dto.getNovelId()) == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_NOT_FOUND);
        }

        // 检查分类是否存在
        if (categoryMapper.selectById(dto.getCategoryId()) == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_CATEGORY_NOT_FOUND);
        }

        // 删除原有分类
        LambdaQueryWrapper<NovelCategoryRelation> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(NovelCategoryRelation::getNovelId, dto.getNovelId());
        relationMapper.delete(deleteWrapper);

        // 添加新分类
        NovelCategoryRelation relation = new NovelCategoryRelation();
        BeanUtils.copyProperties(dto, relation);
        relationMapper.insert(relation);

        return Result.success("设置成功");
    }

    @Override
    public Result getNovelCategory(Integer novelId) {
        log.info("获取小说分类：小说ID={}", novelId);

        LambdaQueryWrapper<NovelCategoryRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelCategoryRelation::getNovelId, novelId);
        NovelCategoryRelation relation = relationMapper.selectOne(queryWrapper);

        if (relation == null) {
            return Result.success(null);
        }

        NovelCategory category = categoryMapper.selectById(relation.getCategoryId());
        if (category == null) {
            return Result.success(null);
        }

        return Result.success(convertToVO(category));
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