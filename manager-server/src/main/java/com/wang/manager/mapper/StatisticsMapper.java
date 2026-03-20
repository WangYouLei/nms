package com.wang.manager.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 统计分析Mapper
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