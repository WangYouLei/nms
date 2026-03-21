package com.wang.manager.service.impl;

import com.wang.common.result.Result;
import com.wang.manager.mapper.StatisticsMapper;
import com.wang.manager.service.DashboardService;
import com.wang.pojo.vo.AuthorRankingVO;
import com.wang.pojo.vo.AuthorStatisticsVO;
import com.wang.pojo.vo.DashboardOverviewVO;
import com.wang.pojo.vo.NovelRankingVO;
import com.wang.pojo.vo.NovelStatisticsVO;
import com.wang.pojo.vo.TrendVO;
import com.wang.pojo.vo.VisitorStatisticsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据概览Service实现
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    private final StatisticsMapper statisticsMapper;

    public DashboardServiceImpl(StatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    // ==================== 概览 ====================

    @Override
    public Result getOverview() {
        log.info("获取数据概览");

        LocalDate today = LocalDate.now();

        DashboardOverviewVO vo = new DashboardOverviewVO();
        
        // 基础统计
        vo.setNovelCount(statisticsMapper.countAllNovels());
        vo.setAuthorCount(statisticsMapper.countAllAuthors());
        vo.setVisitorCount(statisticsMapper.countAllVisitors());
        vo.setCategoryCount(statisticsMapper.countAllCategories());
        
        // 今日新增
        vo.setTodayNewNovels(statisticsMapper.countTodayNewNovels(today));
        vo.setTodayNewAuthors(statisticsMapper.countTodayNewAuthors(today));
        vo.setTodayNewVisitors(statisticsMapper.countTodayNewVisitors(today));
        
        // 小说状态统计
        vo.setHotNovelCount(statisticsMapper.countHotNovels());
        vo.setFinishedNovelCount(statisticsMapper.countFinishedNovels());

        log.info("数据概览获取成功：小说={}, 作者={}, 用户={}", 
                vo.getNovelCount(), vo.getAuthorCount(), vo.getVisitorCount());

        return Result.success(vo);
    }

    // ==================== 统计相关 ====================

    @Override
    public Result getNovelCountStatistics(String groupBy) {
        log.info("小说数量统计：groupBy={}", groupBy);

        if (groupBy == null || groupBy.isBlank()) {
            return Result.error("groupBy参数不能为空");
        }

        List<LinkedHashMap<String, Object>> dataList;
        
        switch (groupBy.toLowerCase()) {
            case "category":
                dataList = statisticsMapper.countNovelsByCategory();
                break;
            case "channel":
                dataList = statisticsMapper.countNovelsByChannel();
                break;
            case "status":
                dataList = statisticsMapper.countNovelsByStatus();
                break;
            case "hot":
                dataList = statisticsMapper.countNovelsByHot();
                break;
            default:
                return Result.error("不支持的分组维度：" + groupBy + "，支持：category/channel/status/hot");
        }

        NovelStatisticsVO vo = new NovelStatisticsVO();
        List<NovelStatisticsVO.Item> items = dataList.stream()
                .map(map -> {
                    NovelStatisticsVO.Item item = new NovelStatisticsVO.Item();
                    item.setName((String) map.get("name"));
                    Object count = map.get("count");
                    item.setCount(count instanceof Number ? ((Number) count).longValue() : 0L);
                    return item;
                })
                .collect(Collectors.toList());
        vo.setItems(items);

        log.info("小说数量统计完成：groupBy={}, 结果数={}", groupBy, items.size());
        return Result.success(vo);
    }

    @Override
    public Result getAuthorCountStatistics() {
        log.info("作者数量统计（按等级）");

        List<LinkedHashMap<String, Object>> dataList = statisticsMapper.countAuthorsByRank();

        AuthorStatisticsVO vo = new AuthorStatisticsVO();
        List<AuthorStatisticsVO.Item> items = dataList.stream()
                .map(map -> {
                    AuthorStatisticsVO.Item item = new AuthorStatisticsVO.Item();
                    Object rank = map.get("rank");
                    item.setRank(rank instanceof Number ? ((Number) rank).intValue() : 0);
                    item.setRankName((String) map.get("rankName"));
                    Object count = map.get("count");
                    item.setCount(count instanceof Number ? ((Number) count).longValue() : 0L);
                    return item;
                })
                .collect(Collectors.toList());
        vo.setItems(items);

        log.info("作者数量统计完成：结果数={}", items.size());
        return Result.success(vo);
    }

    @Override
    public Result getVisitorCountStatistics() {
        log.info("用户数量统计（按VIP等级）");

        List<LinkedHashMap<String, Object>> dataList = statisticsMapper.countVisitorsByVipLevel();

        VisitorStatisticsVO vo = new VisitorStatisticsVO();
        List<VisitorStatisticsVO.Item> items = dataList.stream()
                .map(map -> {
                    VisitorStatisticsVO.Item item = new VisitorStatisticsVO.Item();
                    Object vipLevel = map.get("vipLevel");
                    item.setVipLevel(vipLevel instanceof Number ? ((Number) vipLevel).intValue() : 0);
                    item.setVipName((String) map.get("vipName"));
                    Object count = map.get("count");
                    item.setCount(count instanceof Number ? ((Number) count).longValue() : 0L);
                    return item;
                })
                .collect(Collectors.toList());
        vo.setItems(items);

        log.info("用户数量统计完成：结果数={}", items.size());
        return Result.success(vo);
    }

    // ==================== 排行榜 ====================

    @Override
    public Result getNovelOngoingRanking(Integer limit) {
        log.info("连载榜：limit={}", limit);

        List<LinkedHashMap<String, Object>> dataList = statisticsMapper.rankNovelsByOngoing(limit);

        NovelRankingVO vo = new NovelRankingVO();
        List<NovelRankingVO.Item> items = new java.util.ArrayList<>();
        
        int rank = 1;
        for (LinkedHashMap<String, Object> map : dataList) {
            NovelRankingVO.Item item = new NovelRankingVO.Item();
            item.setRank(rank++);
            item.setId(getIntValue(map, "id"));
            item.setName((String) map.get("name"));
            item.setAuthorName((String) map.get("authorName"));
            item.setChapterCount(getIntValue(map, "chapterCount"));
            item.setIsFinished(getBoolValue(map, "isFinished"));
            item.setIsHot(getBoolValue(map, "isHot"));
            item.setUrl((String) map.get("url"));
            item.setUpdateTime((String) map.get("updateTime"));
            items.add(item);
        }
        vo.setItems(items);

        log.info("连载榜获取完成：结果数={}", items.size());
        return Result.success(vo);
    }

    @Override
    public Result getAuthorProductiveRanking(Integer limit) {
        log.info("作者高产榜：limit={}", limit);

        List<LinkedHashMap<String, Object>> dataList = statisticsMapper.rankAuthorsByProductive(limit);

        AuthorRankingVO vo = new AuthorRankingVO();
        List<AuthorRankingVO.Item> items = new java.util.ArrayList<>();

        int rank = 1;
        for (LinkedHashMap<String, Object> map : dataList) {
            AuthorRankingVO.Item item = new AuthorRankingVO.Item();
            item.setRank(rank++);
            item.setId(getIntValue(map, "id"));
            item.setName((String) map.get("name"));
            item.setAuthorRank(getIntValue(map, "authorRank"));
            item.setRankName((String) map.get("rankName"));
            item.setNovelCount(getIntValue(map, "novelCount"));
            item.setAvatar((String) map.get("avatar"));
            items.add(item);
        }
        vo.setItems(items);

        log.info("作者高产榜获取完成：结果数={}", items.size());
        return Result.success(vo);
    }

    // ==================== 趋势统计 ====================

    @Override
    public Result getNovelTrend(LocalDate startDate, LocalDate endDate, String type) {
        log.info("小说趋势统计：startDate={}, endDate={}, type={}", startDate, endDate, type);
        return buildTrendResult(startDate, endDate, type, "小说",
                statisticsMapper::novelTrendByDay,
                statisticsMapper::novelTrendByWeek,
                statisticsMapper::novelTrendByMonth,
                statisticsMapper::novelTrendByYear);
    }

    @Override
    public Result getAuthorTrend(LocalDate startDate, LocalDate endDate, String type) {
        log.info("作者注册趋势统计：startDate={}, endDate={}, type={}", startDate, endDate, type);
        return buildTrendResult(startDate, endDate, type, "作者注册",
                statisticsMapper::authorTrendByDay,
                statisticsMapper::authorTrendByWeek,
                statisticsMapper::authorTrendByMonth,
                statisticsMapper::authorTrendByYear);
    }

    @Override
    public Result getVisitorTrend(LocalDate startDate, LocalDate endDate, String type) {
        log.info("用户注册趋势统计：startDate={}, endDate={}, type={}", startDate, endDate, type);
        return buildTrendResult(startDate, endDate, type, "用户注册",
                statisticsMapper::visitorTrendByDay,
                statisticsMapper::visitorTrendByWeek,
                statisticsMapper::visitorTrendByMonth,
                statisticsMapper::visitorTrendByYear);
    }

    // ==================== 私有方法 ====================

    @FunctionalInterface
    private interface TrendQuery {
        List<LinkedHashMap<String, Object>> query(LocalDate start, LocalDate end);
    }

    private Result buildTrendResult(LocalDate startDate, LocalDate endDate, String type,
                                      String trendName,
                                      TrendQuery dayQuery,
                                      TrendQuery weekQuery,
                                      TrendQuery monthQuery,
                                      TrendQuery yearQuery) {
        if (startDate == null || endDate == null) {
            return Result.error("日期参数不能为空");
        }
        if (startDate.isAfter(endDate)) {
            return Result.error("开始日期不能晚于结束日期");
        }

        if (type == null || type.isBlank()) {
            type = "month";
        }

        List<LinkedHashMap<String, Object>> dataList;
        switch (type.toLowerCase()) {
            case "day":
                dataList = dayQuery.query(startDate, endDate);
                break;
            case "week":
                dataList = weekQuery.query(startDate, endDate);
                break;
            case "month":
                dataList = monthQuery.query(startDate, endDate);
                break;
            case "year":
                dataList = yearQuery.query(startDate, endDate);
                break;
            default:
                return Result.error("不支持的统计粒度：" + type + "，支持：day/week/month/year");
        }

        TrendVO vo = new TrendVO();
        List<TrendVO.Item> items = dataList.stream()
                .map(map -> {
                    TrendVO.Item item = new TrendVO.Item();
                    Object date = map.get("date");
                    item.setDate(date != null ? date.toString() : "");
                    Object count = map.get("count");
                    item.setCount(count instanceof Number ? ((Number) count).longValue() : 0L);
                    return item;
                })
                .collect(Collectors.toList());
        vo.setItems(items);

        log.info("{}趋势统计完成：type={}, 结果数={}", trendName, type, items.size());
        return Result.success(vo);
    }

    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number num) {
            return num.intValue();
        }
        return null;
    }

    private Boolean getBoolValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Number num) {
            return num.intValue() == 1;
        }
        return false;
    }
}