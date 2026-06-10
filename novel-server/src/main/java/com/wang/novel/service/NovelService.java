package com.wang.novel.service;

import com.wang.common.result.Result;
import com.wang.pojo.dto.NovelDTO;
import com.wang.pojo.dto.NovelSearchDTO;

import java.util.List;

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
    Result getNovelDetail(Long novelId);

    /**
     * 分页搜索小说列表（统一接口）
     * 权限控制：
     * - Author: 只能搜索自己的小说
     * - Manager/Visitor: 可以搜索所有小说
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
    Result deleteNovel(Long id);

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
    Result getHotNovels(Integer pageNum, Integer pageSize, Long categoryId);

    /**
     * 按分类查询小说
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param categoryId 分类ID
     * @param sortBy 排序方式（update/collect/word）
     * @param isFinished 是否完结
     * @return 小说列表
     */
    Result getNovelsByCategory(Integer pageNum, Integer pageSize, Long categoryId, String sortBy, Boolean isFinished);

    /**
     * 获取作者详情（访客端）
     * @param authorId 作者ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 作者详情
     */
    Result getAuthorDetail(Long authorId, Integer pageNum, Integer pageSize);

    /**
     * 获取小说的作者ID（供其他微服务Feign调用）
     * @param novelId 小说ID
     * @return 作者ID
     */
    Result getNovelAuthorId(Long novelId);

    /**
     * 批量获取小说的作者ID（供其他微服务Feign调用，解决N+1查询问题）
     * @param novelIds 小说ID列表
     * @return 小说ID与作者ID的映射
     */
    Result batchGetNovelAuthorIds(List<Long> novelIds);

    /**
     * 获取小说基本信息（供其他微服务Feign调用，用于收藏时填充冗余字段）
     * @param novelId 小说ID
     * @return 小说基本信息
     */
    Result getNovelBasicInfo(Long novelId);
}