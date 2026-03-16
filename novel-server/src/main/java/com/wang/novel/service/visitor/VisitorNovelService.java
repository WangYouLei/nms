package com.wang.novel.service.visitor;

import com.wang.common.result.Result;

/**
 * 访客端小说服务接口
 */
public interface VisitorNovelService {

    /**
     * 分页查询小说列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param keyword 搜索关键词
     * @return 小说列表
     */
    Result getNovelList(Integer pageNum, Integer pageSize, String keyword);

    /**
     * 分页查询热门小说
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param categoryId 分类ID（可选）
     * @return 热门小说列表
     */
    Result getHotNovels(Integer pageNum, Integer pageSize, Integer categoryId);

    /**
     * 获取小说详情
     * @param novelId 小说ID
     * @return 小说详情
     */
    Result getNovelDetail(Integer novelId);

    /**
     * 按分类查询小说
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param categoryId 分类ID
     * @return 小说列表
     */
    Result getNovelsByCategory(Integer pageNum, Integer pageSize, Integer categoryId);

}