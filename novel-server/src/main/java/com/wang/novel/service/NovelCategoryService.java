package com.wang.novel.service;

import com.wang.common.result.Result;
import com.wang.pojo.dto.NovelCategoryDTO;
import com.wang.pojo.dto.NovelCategoryRelationDTO;

/**
 * 小说分类服务接口
 * 提供 author、manager、visitor 三个端口共用的分类功能
 */
public interface NovelCategoryService {

    // ==================== Common - 公共方法 ====================

    /**
     * 获取所有分类
     * @return 分类列表
     */
    Result getAllCategories();

    /**
     * 根据频道获取分类
     * @param category 频道：1-男频, 2-女频
     * @return 分类列表
     */
    Result getCategoriesByChannel(Integer category);

    /**
     * 获取热门分类
     * @return 热门分类列表
     */
    Result getHotCategories();

    /**
     * 根据ID查询分类
     * @param id 分类ID
     * @return 分类信息
     */
    Result getCategoryById(Integer id);

    // ==================== Manager - 管理端方法 ====================

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
     * 获取分类列表（分页）
     */
    Result getCategoryList(Integer pageNum, Integer pageSize, String type, Integer category);

    /**
     * 设置小说分类
     */
    Result setNovelCategory(NovelCategoryRelationDTO dto);

    /**
     * 获取小说的分类
     */
    Result getNovelCategory(Integer novelId);
}