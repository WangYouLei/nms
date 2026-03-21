package com.wang.novel.service;

import com.wang.common.result.Result;
import com.wang.pojo.dto.NovelDTO;
import com.wang.pojo.dto.NovelSearchDTO;

/**
 * 小说服务接口
 * 提供 author、manager、visitor 三个端口共用的小说功能
 */
public interface NovelService {

    // ==================== Common - 公共方法 ====================

    /**
     * 获取小说详情
     * @param novelId 小说ID
     * @return 小说详情
     */
    Result getNovelDetail(Integer novelId);

    /**
     * 分页搜索小说列表（统一接口）
     * <p>
     * 权限控制：
     * - Author: 只能搜索自己的小说
     * - Manager/Visitor: 可以搜索所有小说
     * <p>
     * 搜索条件：
     * - keyword: 关键词搜索（模糊匹配名称、副名称、标签）
     * - name/subName/isHot/isFinished: 精确条件筛选
     *
     * @param dto 搜索条件
     * @return 分页查询结果
     */
    Result searchNovels(NovelSearchDTO dto);

    // ==================== Author - 作者端方法 ====================

    /**
     * 新增小说
     * @param novel 小说信息
     * @return 操作结果
     */
    Result addNovel(NovelDTO novel);

    /**
     * 根据ID删除小说（逻辑删除）
     * @param id 小说ID
     * @return 操作结果
     */
    Result deleteNovel(Integer id);

    /**
     * 修改小说信息
     * @param novelDTO 小说信息
     * @return 操作结果
     */
    Result updateNovel(NovelDTO novelDTO);

    // ==================== Visitor - 访客端方法 ====================

    /**
     * 分页查询热门小说
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param categoryId 分类ID（可选）
     * @return 热门小说列表
     */
    Result getHotNovels(Integer pageNum, Integer pageSize, Integer categoryId);

    /**
     * 按分类查询小说
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param categoryId 分类ID
     * @return 小说列表
     */
    Result getNovelsByCategory(Integer pageNum, Integer pageSize, Integer categoryId);
}