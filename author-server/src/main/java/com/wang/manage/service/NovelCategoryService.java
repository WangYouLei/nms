package com.wang.manage.service;

import com.wang.common.result.Result;
import com.wang.pojo.dto.NovelCategoryDTO;
import com.wang.pojo.dto.NovelCategoryRelationDTO;

/**
 * 小说分类服务接口
 */
public interface NovelCategoryService {

    /**
     * 添加分类
     */
    Result addCategory(NovelCategoryDTO dto);

    /**
     * 修改分类
     */
    Result updateCategory(NovelCategoryDTO dto);

    /**
     * 删除分类
     */
    Result deleteCategory(Integer id);

    /**
     * 根据ID查询分类
     */
    Result getCategoryById(Integer id);

    /**
     * 查询所有分类
     */
    Result getAllCategories();

    /**
     * 根据频道查询分类
     */
    Result getCategoriesByCategory(Integer category);

    /**
     * 查询热门分类
     */
    Result getHotCategories();

    /**
     * 设置小说分类
     */
    Result setNovelCategory(NovelCategoryRelationDTO dto);

    /**
     * 获取小说的分类
     */
    Result getNovelCategory(Integer novelId);

    /**
     * 获取分类列表（分页）
     */
    Result getCategoryList(Integer pageNum, Integer pageSize, String type, Integer category);
}