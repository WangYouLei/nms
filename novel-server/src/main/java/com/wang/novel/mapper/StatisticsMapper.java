package com.wang.novel.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 统计分析Mapper
 * 用于统计分析模块的数据库查询
 */
@Mapper
public interface StatisticsMapper {

    /**
     * 按等级统计作者数量
     * @return 等级统计列表 [{rank, count}]
     */
    List<LinkedHashMap<String, Object>> countAuthorsByRank();

    /**
     * 统计作者总数（未删除）
     * @return 作者总数
     */
    Long countAllAuthors();

    /**
     * 统计今日新增作者
     * @return 今日新增数量
     */
    Long countTodayNewAuthors();

    // ==================== 用户统计 ====================

    /**
     * 按VIP等级统计用户数量
     * @return VIP等级统计列表 [{vipLevel, vipName, count}]
     */
    List<LinkedHashMap<String, Object>> countVisitorsByVipLevel();

    /**
     * 统计用户总数（未删除）
     * @return 用户总数
     */
    Long countAllVisitors();

    /**
     * 统计今日新增用户
     * @return 今日新增数量
     */
    Long countTodayNewVisitors();

    // ==================== 小说排行榜 ====================

    /**
     * 连载榜
     * @param limit 返回数量
     * @return 小说列表
     */
    List<LinkedHashMap<String, Object>> rankNovelsByOngoing(Integer limit);

    // ==================== 作者排行榜 ====================

    /**
     * 作者高产榜（作品数量）
     * @param limit 返回数量
     * @return 作者列表
     */
    List<LinkedHashMap<String, Object>> rankAuthorsByProductive(Integer limit);

    // ==================== 趋势统计 ====================

    /**
     * 小说创建趋势（按天）
     */
    List<LinkedHashMap<String, Object>> novelTrendByDay(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 小说创建趋势（按周）
     */
    List<LinkedHashMap<String, Object>> novelTrendByWeek(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 小说创建趋势（按月）
     */
    List<LinkedHashMap<String, Object>> novelTrendByMonth(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 小说创建趋势（按年）
     */
    List<LinkedHashMap<String, Object>> novelTrendByYear(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 作者注册趋势（按天）
     */
    List<LinkedHashMap<String, Object>> authorTrendByDay(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 作者注册趋势（按周）
     */
    List<LinkedHashMap<String, Object>> authorTrendByWeek(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 作者注册趋势（按月）
     */
    List<LinkedHashMap<String, Object>> authorTrendByMonth(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 作者注册趋势（按年）
     */
    List<LinkedHashMap<String, Object>> authorTrendByYear(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 用户注册趋势（按天）
     */
    List<LinkedHashMap<String, Object>> visitorTrendByDay(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 用户注册趋势（按周）
     */
    List<LinkedHashMap<String, Object>> visitorTrendByWeek(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 用户注册趋势（按月）
     */
    List<LinkedHashMap<String, Object>> visitorTrendByMonth(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 用户注册趋势（按年）
     */
    List<LinkedHashMap<String, Object>> visitorTrendByYear(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}