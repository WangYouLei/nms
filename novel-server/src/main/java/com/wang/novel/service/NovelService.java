package com.wang.novel.service;

import com.wang.common.result.Result;
import com.wang.pojo.dto.NovelDTO;
import com.wang.pojo.dto.NovelSearchDTO;

import java.time.LocalDate;

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

    // ==================== Author/Manager - 作者/管理端方法 ====================

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
     * 分页查询当前登录作者的小说
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页查询结果
     */
    Result getNovelList(Integer pageNum, Integer pageSize);

    /**
     * 搜索小说
     * @param dto 搜索条件
     * @return 分页查询结果
     */
    Result searchNovels(NovelSearchDTO dto);

    /**
     * 修改小说信息
     * @param novelDTO 小说信息
     * @return 操作结果
     */
    Result updateNovel(NovelDTO novelDTO);

    // ==================== Visitor - 访客端方法 ====================

    /**
     * 分页查询小说列表（访客端）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param keyword 搜索关键词
     * @return 小说列表
     */
    Result getVisitorNovelList(Integer pageNum, Integer pageSize, String keyword);

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

    // ==================== Manager - 统计分析 ====================

    /**
     * 获取小说数量统计
     * @param groupBy 分组维度：category/channel/status/hot
     * @return 统计结果
     */
    Result getNovelCountStatistics(String groupBy);

    /**
     * 获取作者数量统计（按等级）
     * @return 统计结果
     */
    Result getAuthorCountStatistics();

    /**
     * 获取用户数量统计（按VIP等级）
     * @return 统计结果
     */
    Result getVisitorCountStatistics();

    // ==================== Manager - 小说排行榜 ====================

    /**
     * 连载榜
     * @param limit 返回数量
     * @return 排行榜数据
     */
    Result getNovelOngoingRanking(Integer limit);

    // ==================== Manager - 作者排行榜 ====================

    /**
     * 作者高产榜（作品数量）
     * @param limit 返回数量
     * @return 排行榜数据
     */
    Result getAuthorProductiveRanking(Integer limit);

    // ==================== Manager - 趋势统计 ====================

    /**
     * 小说趋势统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param type 时间粒度：day/week/month/year
     * @return 趋势数据
     */
    Result getNovelTrend(LocalDate startDate, LocalDate endDate, String type);

    /**
     * 作者注册趋势统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param type 时间粒度：day/week/month/year
     * @return 趋势数据
     */
    Result getAuthorTrend(LocalDate startDate, LocalDate endDate, String type);

    /**
     * 用户注册趋势统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param type 时间粒度：day/week/month/year
     * @return 趋势数据
     */
    Result getVisitorTrend(LocalDate startDate, LocalDate endDate, String type);
}