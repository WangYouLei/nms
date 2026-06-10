package com.wang.manager.service.impl;

import com.wang.common.result.Result;
import com.wang.common.service.CacheService;
import com.wang.common.constants.CacheConstants;
import com.wang.common.model.ZSetEntry;
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
import java.util.ArrayList;
import java.util.HashMap;
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
    private final CacheService cacheService;
    private static final int RANKING_LIMIT_MAX = 100;

    public DashboardServiceImpl(StatisticsMapper statisticsMapper, CacheService cacheService) {
        this.statisticsMapper = statisticsMapper;
        this.cacheService = cacheService;
    }

    // ==================== 概览 ====================

    @Override
    public Result getOverview() {
        log.info("获取数据概览");

        // 先从缓存获取
        DashboardOverviewVO cachedVo = cacheService.get(CacheConstants.DASHBOARD_OVERVIEW, DashboardOverviewVO.class);
        if (cachedVo != null) {
            log.info("从缓存获取数据概览");
            return Result.success(cachedVo);
        }

        // 缓存未命中，查询数据库
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

        // 存入缓存
        cacheService.set(CacheConstants.DASHBOARD_OVERVIEW, vo, CacheConstants.DASHBOARD_OVERVIEW_TTL);
        log.info("数据概览已缓存");

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

        // 先从缓存获取
        String cacheKey = CacheConstants.buildStatsKey("novel:" + groupBy);
        NovelStatisticsVO cachedVo = cacheService.get(cacheKey, NovelStatisticsVO.class);
        if (cachedVo != null) {
            log.info("从缓存获取小说数量统计：groupBy={}", groupBy);
            return Result.success(cachedVo);
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

        // 存入缓存
        cacheService.set(cacheKey, vo, CacheConstants.STATS_TTL);
        log.info("小说数量统计已缓存：groupBy={}", groupBy);

        log.info("小说数量统计完成：groupBy={}, 结果数={}", groupBy, items.size());
        return Result.success(vo);
    }

    @Override
    public Result getAuthorCountStatistics() {
        log.info("作者数量统计（按等级）");

        // 先从缓存获取
        String cacheKey = CacheConstants.buildStatsKey("author:rank");
        AuthorStatisticsVO cachedVo = cacheService.get(cacheKey, AuthorStatisticsVO.class);
        if (cachedVo != null) {
            log.info("从缓存获取作者数量统计");
            return Result.success(cachedVo);
        }

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

        // 存入缓存
        cacheService.set(cacheKey, vo, CacheConstants.STATS_TTL);
        log.info("作者数量统计已缓存");

        log.info("作者数量统计完成：结果数={}", items.size());
        return Result.success(vo);
    }

    @Override
    public Result getVisitorCountStatistics() {
        log.info("用户数量统计（按VIP等级）");

        // 先从缓存获取
        String cacheKey = CacheConstants.buildStatsKey("visitor:vip");
        VisitorStatisticsVO cachedVo = cacheService.get(cacheKey, VisitorStatisticsVO.class);
        if (cachedVo != null) {
            log.info("从缓存获取用户数量统计");
            return Result.success(cachedVo);
        }

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

        // 存入缓存
        cacheService.set(cacheKey, vo, CacheConstants.STATS_TTL);
        log.info("用户数量统计已缓存");

        log.info("用户数量统计完成：结果数={}", items.size());
        return Result.success(vo);
    }

    // ==================== 排行榜 ====================

    @Override
    public Result getNovelOngoingRanking(Integer limit) {
        log.info("连载榜：limit={}", limit);
        return buildNovelRankingFromZSet(CacheConstants.RANKING_NOVEL_ONGOING, limit, "连载榜");
    }

    @Override
    public Result getAuthorProductiveRanking(Integer limit) {
        log.info("作者高产榜：limit={}", limit);

        limit = sanitizeLimit(limit);

        // 从 Redis ZSET 读取排行榜
        List<ZSetEntry> entries = cacheService.zRevRangeWithScores(
                CacheConstants.RANKING_AUTHOR_PRODUCTIVE, 0, limit - 1);

        if (entries.isEmpty()) {
            log.info("作者高产榜 ZSET 为空，返回空列表");
            AuthorRankingVO vo = new AuthorRankingVO();
            vo.setItems(new ArrayList<>());
            return Result.success(vo);
        }

        // 批量获取作者详情
        List<Long> authorIds = entries.stream()
                .map(e -> Long.parseLong(e.getMember()))
                .collect(Collectors.toList());
        List<LinkedHashMap<String, Object>> dataList = statisticsMapper.batchGetAuthorsByIds(authorIds);

        // 构建详情映射
        Map<Long, LinkedHashMap<String, Object>> detailMap = new HashMap<>();
        for (LinkedHashMap<String, Object> map : dataList) {
            Long id = getLongValue(map, "id");
            if (id != null) {
                detailMap.put(id, map);
            }
        }

        // 组装排行榜 VO
        AuthorRankingVO vo = new AuthorRankingVO();
        List<AuthorRankingVO.Item> items = new ArrayList<>();
        int rank = 1;
        for (ZSetEntry entry : entries) {
            Long authorId = Long.parseLong(entry.getMember());
            LinkedHashMap<String, Object> detail = detailMap.get(authorId);
            if (detail == null) {
                continue;
            }
            AuthorRankingVO.Item item = new AuthorRankingVO.Item();
            item.setRank(rank++);
            item.setId(authorId);
            item.setName((String) detail.get("name"));
            item.setAuthorRank(getIntValue(detail, "authorRank"));
            item.setRankName((String) detail.get("rankName"));
            item.setNovelCount(getIntValue(detail, "novelCount"));
            item.setAvatar((String) detail.get("avatar"));
            items.add(item);
        }
        vo.setItems(items);

        log.info("作者高产榜获取完成：结果数={}", items.size());
        return Result.success(vo);
    }

    @Override
    public Result getNovelCollectRanking(Integer limit) {
        log.info("小说收藏榜：limit={}", limit);
        return buildNovelRankingFromZSet(CacheConstants.RANKING_NOVEL_COLLECT, limit, "小说收藏榜");
    }

    @Override
    public Result getNovelLatestRanking(Integer limit) {
        log.info("最新更新榜：limit={}", limit);
        return buildNovelRankingFromZSet(CacheConstants.RANKING_NOVEL_LATEST, limit, "最新更新榜");
    }

    @Override
    public Result getNovelNewRanking(Integer limit) {
        log.info("新书榜：limit={}", limit);
        return buildNovelRankingFromZSet(CacheConstants.RANKING_NOVEL_NEW, limit, "新书榜");
    }

    // ==================== 趋势统计 ====================

    @Override
    public Result getNovelTrend(LocalDate startDate, LocalDate endDate, String type) {
        log.info("小说趋势统计：startDate={}, endDate={}, type={}", startDate, endDate, type);
        
        // 先从缓存获取
        String cacheKey = CacheConstants.buildTrendKey("novel", startDate.toString(), endDate.toString() + ":" + type);
        TrendVO cachedVo = cacheService.get(cacheKey, TrendVO.class);
        if (cachedVo != null) {
            log.info("从缓存获取小说趋势统计");
            return Result.success(cachedVo);
        }
        
        Result result = buildTrendResult(startDate, endDate, type, "小说",
                statisticsMapper::novelTrendByDay,
                statisticsMapper::novelTrendByWeek,
                statisticsMapper::novelTrendByMonth,
                statisticsMapper::novelTrendByYear);
        
        // 存入缓存
        if (result.getCode() == 10000) {
            cacheService.set(cacheKey, result.getData(), CacheConstants.TREND_TTL);
            log.info("小说趋势统计已缓存");
        }
        
        return result;
    }

    @Override
    public Result getAuthorTrend(LocalDate startDate, LocalDate endDate, String type) {
        log.info("作者注册趋势统计：startDate={}, endDate={}, type={}", startDate, endDate, type);
        
        // 先从缓存获取
        String cacheKey = CacheConstants.buildTrendKey("author", startDate.toString(), endDate.toString() + ":" + type);
        TrendVO cachedVo = cacheService.get(cacheKey, TrendVO.class);
        if (cachedVo != null) {
            log.info("从缓存获取作者注册趋势统计");
            return Result.success(cachedVo);
        }
        
        Result result = buildTrendResult(startDate, endDate, type, "作者注册",
                statisticsMapper::authorTrendByDay,
                statisticsMapper::authorTrendByWeek,
                statisticsMapper::authorTrendByMonth,
                statisticsMapper::authorTrendByYear);
        
        // 存入缓存
        if (result.getCode() == 10000) {
            cacheService.set(cacheKey, result.getData(), CacheConstants.TREND_TTL);
            log.info("作者注册趋势统计已缓存");
        }
        
        return result;
    }

    @Override
    public Result getVisitorTrend(LocalDate startDate, LocalDate endDate, String type) {
        log.info("用户注册趋势统计：startDate={}, endDate={}, type={}", startDate, endDate, type);
        
        // 先从缓存获取
        String cacheKey = CacheConstants.buildTrendKey("visitor", startDate.toString(), endDate.toString() + ":" + type);
        TrendVO cachedVo = cacheService.get(cacheKey, TrendVO.class);
        if (cachedVo != null) {
            log.info("从缓存获取用户注册趋势统计");
            return Result.success(cachedVo);
        }
        
        Result result = buildTrendResult(startDate, endDate, type, "用户注册",
                statisticsMapper::visitorTrendByDay,
                statisticsMapper::visitorTrendByWeek,
                statisticsMapper::visitorTrendByMonth,
                statisticsMapper::visitorTrendByYear);
        
        // 存入缓存
        if (result.getCode() == 10000) {
            cacheService.set(cacheKey, result.getData(), CacheConstants.TREND_TTL);
            log.info("用户注册趋势统计已缓存");
        }
        
        return result;
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

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number num) {
            return num.longValue();
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

    private int sanitizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 10;
        }
        return Math.min(limit, RANKING_LIMIT_MAX);
    }

    private Result buildNovelRankingFromZSet(String zsetKey, Integer limit, String rankingName) {
        limit = sanitizeLimit(limit);

        List<ZSetEntry> entries = cacheService.zRevRangeWithScores(zsetKey, 0, limit - 1);
        if (entries.isEmpty()) {
            NovelRankingVO vo = new NovelRankingVO();
            vo.setItems(new ArrayList<>());
            return Result.success(vo);
        }

        List<Long> novelIds = entries.stream()
                .map(e -> Long.parseLong(e.getMember()))
                .collect(Collectors.toList());
        List<LinkedHashMap<String, Object>> dataList = statisticsMapper.batchGetNovelsByIds(novelIds);

        Map<Long, LinkedHashMap<String, Object>> detailMap = new HashMap<>();
        for (LinkedHashMap<String, Object> map : dataList) {
            Long id = getLongValue(map, "id");
            if (id != null) {
                detailMap.put(id, map);
            }
        }

        NovelRankingVO vo = new NovelRankingVO();
        List<NovelRankingVO.Item> items = new ArrayList<>();
        int rank = 1;
        for (ZSetEntry entry : entries) {
            Long novelId = Long.parseLong(entry.getMember());
            LinkedHashMap<String, Object> detail = detailMap.get(novelId);
            if (detail == null) {
                continue;
            }
            NovelRankingVO.Item item = new NovelRankingVO.Item();
            item.setRank(rank++);
            item.setId(novelId);
            item.setName((String) detail.get("name"));
            item.setAuthorName((String) detail.get("authorName"));
            item.setChapterCount(getIntValue(detail, "chapterCount"));
            item.setCollectCount(getIntValue(detail, "collectCount"));
            item.setAllWordCount(getIntValue(detail, "allWordCount"));
            item.setIsFinished(getBoolValue(detail, "isFinished"));
            item.setIsHot(getBoolValue(detail, "isHot"));
            item.setUrl((String) detail.get("url"));
            item.setUpdateTime((String) detail.get("updateTime"));
            item.setScore(entry.getScore());
            items.add(item);
        }
        vo.setItems(items);

        log.info("{}获取完成：结果数={}", rankingName, items.size());
        return Result.success(vo);
    }
}