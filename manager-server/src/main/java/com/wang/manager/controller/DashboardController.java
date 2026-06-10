package com.wang.manager.controller;

import com.wang.common.result.Result;
import com.wang.manager.service.DashboardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 数据概览Controller
 */
@Slf4j
@RestController
@Api(tags = "数据概览")
@RequestMapping("/manager/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // ==================== 概览 ====================

    @GetMapping("/overview")
    @ApiOperation("获取概览数据")
    public Result getOverview() {
        log.info("获取数据概览请求");
        return dashboardService.getOverview();
    }

    // ==================== 统计相关 ====================

    @GetMapping("/statistics/novel/count")
    @ApiOperation("小说数量统计")
    public Result getNovelCountStatistics(
            @ApiParam("分组维度(category/channel/status/hot)") @RequestParam String groupBy) {
        log.info("小说数量统计：groupBy={}", groupBy);
        return dashboardService.getNovelCountStatistics(groupBy);
    }

    @GetMapping("/statistics/author/count")
    @ApiOperation("作者数量统计（按等级）")
    public Result getAuthorCountStatistics() {
        log.info("作者数量统计（按等级）");
        return dashboardService.getAuthorCountStatistics();
    }

    @GetMapping("/statistics/visitor/count")
    @ApiOperation("用户数量统计（按VIP等级）")
    public Result getVisitorCountStatistics() {
        log.info("用户数量统计（按VIP等级）");
        return dashboardService.getVisitorCountStatistics();
    }

    // ==================== 排行榜 ====================

    @GetMapping("/ranking/novel/ongoing")
    @ApiOperation("连载榜")
    public Result getNovelOngoingRanking(
            @ApiParam("返回数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("连载榜：limit={}", limit);
        return dashboardService.getNovelOngoingRanking(limit);
    }

    @GetMapping("/ranking/author/productive")
    @ApiOperation("作者高产榜（作品数量）")
    public Result getAuthorProductiveRanking(
            @ApiParam("返回数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("作者高产榜：limit={}", limit);
        return dashboardService.getAuthorProductiveRanking(limit);
    }

    @GetMapping("/ranking/novel/collect")
    @ApiOperation("小说收藏榜")
    public Result getNovelCollectRanking(
            @ApiParam("返回数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("小说收藏榜：limit={}", limit);
        return dashboardService.getNovelCollectRanking(limit);
    }

    @GetMapping("/ranking/novel/latest")
    @ApiOperation("最新更新榜")
    public Result getNovelLatestRanking(
            @ApiParam("返回数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("最新更新榜：limit={}", limit);
        return dashboardService.getNovelLatestRanking(limit);
    }

    @GetMapping("/ranking/novel/new")
    @ApiOperation("新书榜")
    public Result getNovelNewRanking(
            @ApiParam("返回数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("新书榜：limit={}", limit);
        return dashboardService.getNovelNewRanking(limit);
    }

    // ==================== 趋势统计 ====================

    /**
     * 小说趋势统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param type 统计粒度：day(按天)/week(按周)/month(按月)/year(按年)（时间范围内的数据如何分组）
     * @return 趋势统计数据
     */
    @GetMapping("/statistics/novel/trend")
    @ApiOperation("小说趋势统计")
    public Result getNovelTrend(
            @ApiParam("开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam("结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @ApiParam("统计粒度(day/week/month/year)") @RequestParam(defaultValue = "month") String type) {
        log.info("小说趋势统计：startDate={}, endDate={}, type={}", startDate, endDate, type);
        return dashboardService.getNovelTrend(startDate, endDate, type);
    }

    /**
     * 作者注册趋势统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param type 统计粒度：day(按天)/week(按周)/month(按月)/year(按年)（时间范围内的数据如何分组）
     * @return 趋势统计数据
     */
    @GetMapping("/statistics/author/register")
    @ApiOperation("作者注册趋势统计")
    public Result getAuthorTrend(
            @ApiParam("开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam("结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @ApiParam("统计粒度(day/week/month/year)") @RequestParam(defaultValue = "month") String type) {
        log.info("作者注册趋势统计：startDate={}, endDate={}, type={}", startDate, endDate, type);
        return dashboardService.getAuthorTrend(startDate, endDate, type);
    }

    /**
     * 用户注册趋势统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param type 统计粒度：day(按天)/week(按周)/month(按月)/year(按年)（时间范围内的数据如何分组）
     * @return 趋势统计数据
     */
    @GetMapping("/statistics/visitor/register")
    @ApiOperation("用户注册趋势统计")
    public Result getVisitorTrend(
            @ApiParam("开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam("结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @ApiParam("统计粒度(day/week/month/year)") @RequestParam(defaultValue = "month") String type) {
        log.info("用户注册趋势统计：startDate={}, endDate={}, type={}", startDate, endDate, type);
        return dashboardService.getVisitorTrend(startDate, endDate, type);
    }
}