package com.wang.manager.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

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
}