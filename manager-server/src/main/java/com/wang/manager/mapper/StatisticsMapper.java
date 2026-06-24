package com.wang.manager.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 统计分析Mapper
 *
 * 【跨域只读查询 - CQRS 读模型】
 * 本 Mapper 涉及 novel、author、visitor、novel_category、novel_category_relation 等多张表的聚合查询。
 * 这些查询属于管理后台的统计需求，采用 CQRS 思路，直接查库是合理的妥协方案。
 *
 * 约束：
 * 1. 本 Mapper 只允许执行 SELECT 查询，禁止任何 INSERT/UPDATE/DELETE 操作
 * 2. 如需修改其他服务的数据，必须通过 Feign 调用对应服务的 API
 * 3. 新增统计查询时，必须在此注释中说明涉及的表
 *
 * 涉及的表：
 * - novel（小说表，归属 novel-server）
 * - author（作者表，归属 author-server）
 * - visitor（访客表，归属 visitor-server）
 * - novel_category（分类表，归属 novel-server）
 * - novel_category_relation（分类关联表，归属 novel-server）
 */
@Mapper
public interface StatisticsMapper {

    // ===== 概览统计 =====

    /**
     * 统计小说总数
     */
    Long countAllNovels();

    /**
     * 统计作者总数（未删除）
     */
    Long countAllAuthors();

    /**
     * 统计用户总数（未删除）
     */
    Long countAllVisitors();

    /**
     * 统计分类总数
     */
    Long countAllCategories();

    /**
     * 统计今日新增小说
     */
    Long countTodayNewNovels(@Param("today") LocalDate today);

    /**
     * 统计今日新增作者
     */
    Long countTodayNewAuthors(@Param("today") LocalDate today);

    /**
     * 统计今日新增用户
     */
    Long countTodayNewVisitors(@Param("today") LocalDate today);

    /**
     * 统计本周新增小说
     */
    Long countWeekNewNovels(@Param("today") LocalDate today);

    /**
     * 统计本周新增作者
     */
    Long countWeekNewAuthors(@Param("today") LocalDate today);

    /**
     * 统计本周新增用户
     */
    Long countWeekNewVisitors(@Param("today") LocalDate today);

    /**
     * 统计本月新增小说
     */
    Long countMonthNewNovels(@Param("today") LocalDate today);

    /**
     * 统计本月新增作者
     */
    Long countMonthNewAuthors(@Param("today") LocalDate today);

    /**
     * 统计本月新增用户
     */
    Long countMonthNewVisitors(@Param("today") LocalDate today);

    /**
     * 统计热门小说数
     */
    Long countHotNovels();

    /**
     * 统计完结小说数
     */
    Long countFinishedNovels();

    // ===== 数量统计 =====

    /**
     * 按分类统计小说数量
     */
    List<LinkedHashMap<String, Object>> countNovelsByCategory();

    /**
     * 按频道统计小说数量
     */
    List<LinkedHashMap<String, Object>> countNovelsByChannel();

    /**
     * 按状态统计小说数量（完结/连载）
     */
    List<LinkedHashMap<String, Object>> countNovelsByStatus();

    /**
     * 按热门状态统计小说数量
     */
    List<LinkedHashMap<String, Object>> countNovelsByHot();

    /**
     * 按等级统计作者数量
     */
    List<LinkedHashMap<String, Object>> countAuthorsByRank();

    /**
     * 按VIP等级统计用户数量
     */
    List<LinkedHashMap<String, Object>> countVisitorsByVipLevel();

    // ===== 排行榜 =====

    /**
     * 连载榜
     */
    List<LinkedHashMap<String, Object>> rankNovelsByOngoing(@Param("limit") Integer limit);

    /**
     * 作者高产榜
     */
    List<LinkedHashMap<String, Object>> rankAuthorsByProductive(@Param("limit") Integer limit);

    /**
     * 批量获取小说基本信息（用于排行榜详情填充）
     */
    List<LinkedHashMap<String, Object>> batchGetNovelsByIds(@Param("ids") List<Long> ids);

    /**
     * 批量获取作者基本信息（用于排行榜详情填充）
     */
    List<LinkedHashMap<String, Object>> batchGetAuthorsByIds(@Param("ids") List<Long> ids);

    /**
     * 查询所有小说用于排行榜全量同步
     */
    List<LinkedHashMap<String, Object>> selectAllNovelsForRankingSync();

    /**
     * 查询所有作者用于排行榜全量同步
     */
    List<LinkedHashMap<String, Object>> selectAllAuthorsForRankingSync();

    // ===== 趋势统计 - 小说 =====

    List<LinkedHashMap<String, Object>> novelTrendByDay(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<LinkedHashMap<String, Object>> novelTrendByWeek(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<LinkedHashMap<String, Object>> novelTrendByMonth(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<LinkedHashMap<String, Object>> novelTrendByYear(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // ===== 趋势统计 - 作者 =====

    List<LinkedHashMap<String, Object>> authorTrendByDay(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<LinkedHashMap<String, Object>> authorTrendByWeek(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<LinkedHashMap<String, Object>> authorTrendByMonth(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<LinkedHashMap<String, Object>> authorTrendByYear(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // ===== 趋势统计 - 用户 =====

    List<LinkedHashMap<String, Object>> visitorTrendByDay(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<LinkedHashMap<String, Object>> visitorTrendByWeek(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<LinkedHashMap<String, Object>> visitorTrendByMonth(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<LinkedHashMap<String, Object>> visitorTrendByYear(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}