package com.wang.novel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.utils.RoleContextUtil;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.enums.UserRole;
import com.wang.common.model.LoginUser;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.novel.mapper.NovelCategoryMapper;
import com.wang.novel.mapper.NovelCategoryRelationMapper;
import com.wang.novel.mapper.NovelMapper;
import com.wang.novel.service.NovelCategoryService;
import com.wang.pojo.dto.NovelCategoryDTO;
import com.wang.pojo.dto.NovelCategoryRelationDTO;
import com.wang.pojo.entity.Novel;
import com.wang.pojo.entity.NovelCategory;
import com.wang.pojo.entity.NovelCategoryRelation;
import com.wang.pojo.vo.NovelCategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 小说分类服务实现类
 * 提供 author、manager、visitor 三个端口共用的分类功能
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

    // ==================== Common - 公共方法 ====================

    @Override
    public Result getAllCategories() {
        List<NovelCategory> list = categoryMapper.selectList(null);
        List<NovelCategoryVO> voList = list.stream().map(this::convertToVO).collect(Collectors.toList());
        return Result.success(voList);
    }

    @Override
    public Result getCategoriesByChannel(Integer category) {
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
    public Result getCategoryById(Integer id) {
        NovelCategory entity = categoryMapper.selectById(id);
        if (entity == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_CATEGORY_NOT_FOUND);
        }
        return Result.success(convertToVO(entity));
    }

    // ==================== Manager - 管理端方法 ====================

    @Override
    public Result addCategory(NovelCategoryDTO dto) {
        // 权限校验：只有管理员可以添加分类
        LoginUser loginUser = RoleContextUtil.getCurrentUser();
        if (loginUser == null || !UserRole.MANAGER.equals(loginUser.getRole())) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

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
        // 权限校验：只有管理员可以修改分类
        LoginUser loginUser = RoleContextUtil.getCurrentUser();
        if (loginUser == null || !UserRole.MANAGER.equals(loginUser.getRole())) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

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
        categoryMapper.update(entity);
        return Result.success(convertToVO(entity));
    }

    @Override
    @Transactional
    public Result deleteCategory(Integer id) {
        // 权限校验：只有管理员可以删除分类
        LoginUser loginUser = RoleContextUtil.getCurrentUser();
        if (loginUser == null || !UserRole.MANAGER.equals(loginUser.getRole())) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

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

        PageResult<NovelCategoryVO> pageResult = PageResult.build(
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getTotal(),
                voList
        );
        return Result.success(pageResult);
    }

    @Override
    @Transactional
    public Result setNovelCategory(NovelCategoryRelationDTO dto) {
        // 参数校验
        if (dto.getCategoryIds() == null || dto.getCategoryIds().isEmpty()) {
            return Result.error("分类ID列表不能为空");
        }

        // 检查小说是否存在
        Novel novel = novelMapper.selectById(dto.getNovelId());
        if (novel == null) {
            return Result.buildResult(BizCodeEnum.NOVEL_NOT_FOUND);
        }

        // 权限校验：只有作者可以设置自己小说的分类
        LoginUser loginUser = RoleContextUtil.getCurrentUser();
        if (loginUser == null || !UserRole.AUTHOR.equals(loginUser.getRole())) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }
        if (!Objects.equals(novel.getAuthorId(), loginUser.getId())) {
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        // 批量检查分类是否存在
        List<NovelCategory> categories = categoryMapper.selectBatchIds(dto.getCategoryIds());
        if (categories.size() != dto.getCategoryIds().size()) {
            return Result.error("部分分类不存在");
        }

        // 删除原有分类关联
        LambdaQueryWrapper<NovelCategoryRelation> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(NovelCategoryRelation::getNovelId, dto.getNovelId());
        relationMapper.delete(deleteWrapper);

        // 批量添加新分类关联
        for (Integer categoryId : dto.getCategoryIds()) {
            NovelCategoryRelation relation = new NovelCategoryRelation();
            relation.setNovelId(dto.getNovelId());
            relation.setCategoryId(categoryId);
            relationMapper.insert(relation);
        }

        log.info("设置小说分类成功：小说ID={}, 分类数量={}", dto.getNovelId(), dto.getCategoryIds().size());
        return Result.success("设置成功");
    }

    @Override
    public Result getNovelCategory(Integer novelId) {
        LambdaQueryWrapper<NovelCategoryRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelCategoryRelation::getNovelId, novelId);
        List<NovelCategoryRelation> relations = relationMapper.selectList(queryWrapper);

        if (relations.isEmpty()) {
            return Result.success(null);
        }
        //提取出所有分类ID
        List<Integer> list = relations.stream().map(NovelCategoryRelation::getCategoryId).toList();
        List<NovelCategory> categoryList = categoryMapper.selectBatchIds(list);

        if (categoryList.isEmpty()) {
            return Result.success(null);
        }

        return Result.success(convertToVO(categoryList));
    }

    // ==================== 私有方法 ====================

    /**
     * 转换为VO（单个对象）
     */
    private NovelCategoryVO convertToVO(NovelCategory entity) {
        NovelCategoryVO vo = new NovelCategoryVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setCategoryName(entity.getCategory() == 1 ? "男频" : "女频");
        return vo;
    }

    /**
     * 转换为VO（列表）
     */
    private List<NovelCategoryVO> convertToVO(List<NovelCategory> entitys) {
        List<NovelCategoryVO> vos = new ArrayList<>();
        entitys.forEach(entity -> {
            vos.add(convertToVO(entity));
        });
        return vos;
    }
}