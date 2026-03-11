package com.wang.visitor.service;

import com.wang.common.result.Result;

/**
 * 访客端分类服务接口
 */
public interface VisitorCategoryService {

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
}