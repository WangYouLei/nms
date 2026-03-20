package com.wang.manager.service;

import com.wang.common.result.Result;

import java.time.LocalDate;

/**
 * 数据概览Service
 */
public interface DashboardService {

    /**
     * 获取概览数据
     * @return 概览数据
     */
    Result getOverview();

    // ==================== 统计相关 ====================

    /**
     * 小说数量统计
     * @param groupBy 分组维度（category/channel/status/hot）
     * @return 统计结果
     */
    Result getNovelCountStatistics(String groupBy);

    /**
     * 作者数量统计（按等级）
     * @return 统计结果
     */
    Result getAuthorCountStatistics();

    /**
     * 用户数量统计（按VIP等级）
     * @return 统计结果
     */
    Result getVisitorCountStatistics();

    // ==================== 排行榜 ====================

    /**
     * 连载榜
     * @param limit 返回数量
     * @return 排行榜
     */
    Result getNovelOngoingRanking(Integer limit);

    /**
     * 作者高产榜
     * @param limit 返回数量
     * @return 排行榜
     */
    Result getAuthorProductiveRanking(Integer limit);

    // ==================== 趋势统计 ====================

    /**
     * 小说趋势统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param type 统计粒度（day/week/month/year）
     * @return 趋势数据
     */
    Result getNovelTrend(LocalDate startDate, LocalDate endDate, String type);

    /**
     * 作者注册趋势统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param type 统计粒度（day/week/month/year）
     * @return 趋势数据
     */
    Result getAuthorTrend(LocalDate startDate, LocalDate endDate, String type);

    /**
     * 用户注册趋势统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param type 统计粒度（day/week/month/year）
     * @return 趋势数据
     */
    Result getVisitorTrend(LocalDate startDate, LocalDate endDate, String type);
}