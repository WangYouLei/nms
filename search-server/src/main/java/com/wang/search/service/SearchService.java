package com.wang.search.service;

import com.wang.common.result.Result;
import com.wang.pojo.dto.SearchDTO;

/**
 * 搜索服务接口
 */
public interface SearchService {

    /**
     * 搜索小说（ES全文检索）
     * @param dto 搜索条件
     * @return 分页搜索结果（含高亮）
     */
    Result searchNovels(SearchDTO dto);

    /**
     * 搜索作者
     * @param dto 搜索条件
     * @return 分页搜索结果
     */
    Result searchAuthors(SearchDTO dto);

    /**
     * 搜索建议（自动补全）
     * @param prefix 前缀关键词
     * @return 建议列表
     */
    Result suggest(String prefix);

    /**
     * 分类搜索（聚合统计）
     * @param categoryType 频道类型（可选：1男频/2女频）
     * @return 分类聚合结果
     */
    Result searchCategories(Integer categoryType);
}
