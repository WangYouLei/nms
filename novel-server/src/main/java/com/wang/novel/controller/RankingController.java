package com.wang.novel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wang.common.constants.CacheConstants;
import com.wang.common.model.ZSetEntry;
import com.wang.common.result.Result;
import com.wang.common.service.CacheService;
import com.wang.novel.mapper.NovelMapper;
import com.wang.pojo.entity.Novel;
import com.wang.pojo.vo.AuthorRankingVO;
import com.wang.pojo.vo.NovelRankingVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 访客端排行榜Controller
 * 直接从 Redis ZSET 读取排行榜数据
 */
@Slf4j
@RestController
@Api(tags = "排行榜（访客端）")
@RequestMapping("/visitor/ranking")
public class RankingController {

    private final CacheService cacheService;
    private final NovelMapper novelMapper;

    private static final int RANKING_LIMIT_MAX = 100;

    public RankingController(CacheService cacheService, NovelMapper novelMapper) {
        this.cacheService = cacheService;
        this.novelMapper = novelMapper;
    }

    @GetMapping("/novel/collect")
    @ApiOperation("小说收藏榜")
    public Result getNovelCollectRanking(
            @ApiParam("返回数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("访客端-小说收藏榜：limit={}", limit);
        return buildNovelRanking(CacheConstants.RANKING_NOVEL_COLLECT, limit, "收藏榜");
    }

    @GetMapping("/novel/ongoing")
    @ApiOperation("连载榜")
    public Result getNovelOngoingRanking(
            @ApiParam("返回数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("访客端-连载榜：limit={}", limit);
        return buildNovelRanking(CacheConstants.RANKING_NOVEL_ONGOING, limit, "连载榜");
    }

    @GetMapping("/novel/latest")
    @ApiOperation("最新更新榜")
    public Result getNovelLatestRanking(
            @ApiParam("返回数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("访客端-最新更新榜：limit={}", limit);
        return buildNovelRanking(CacheConstants.RANKING_NOVEL_LATEST, limit, "最新更新榜");
    }

    @GetMapping("/novel/new")
    @ApiOperation("新书榜")
    public Result getNovelNewRanking(
            @ApiParam("返回数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("访客端-新书榜：limit={}", limit);
        return buildNovelRanking(CacheConstants.RANKING_NOVEL_NEW, limit, "新书榜");
    }

    @GetMapping("/author/productive")
    @ApiOperation("作者高产榜")
    public Result getAuthorProductiveRanking(
            @ApiParam("返回数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("访客端-作者高产榜：limit={}", limit);

        if (limit == null || limit < 1) {
            limit = 10;
        }
        limit = Math.min(limit, RANKING_LIMIT_MAX);

        List<ZSetEntry> entries = cacheService.zRevRangeWithScores(
                CacheConstants.RANKING_AUTHOR_PRODUCTIVE, 0, limit - 1);

        AuthorRankingVO vo = new AuthorRankingVO();
        List<AuthorRankingVO.Item> items = new ArrayList<>();
        int rank = 1;
        for (ZSetEntry entry : entries) {
            AuthorRankingVO.Item item = new AuthorRankingVO.Item();
            item.setRank(rank++);
            item.setId(Long.parseLong(entry.getMember()));
            item.setNovelCount(entry.getScore() != null ? entry.getScore().intValue() : 0);
            items.add(item);
        }
        vo.setItems(items);

        log.info("访客端-作者高产榜获取完成：结果数={}", items.size());
        return Result.success(vo);
    }

    private Result buildNovelRanking(String zsetKey, Integer limit, String rankingName) {
        if (limit == null || limit < 1) {
            limit = 10;
        }
        limit = Math.min(limit, RANKING_LIMIT_MAX);

        List<ZSetEntry> entries = cacheService.zRevRangeWithScores(zsetKey, 0, limit - 1);
        if (entries.isEmpty()) {
            NovelRankingVO vo = new NovelRankingVO();
            vo.setItems(new ArrayList<>());
            return Result.success(vo);
        }

        List<Long> novelIds = entries.stream()
                .map(e -> Long.parseLong(e.getMember()))
                .collect(Collectors.toList());
        List<Novel> novels = novelMapper.selectBatchIds(novelIds);

        Map<Long, Novel> novelMap = novels.stream()
                .collect(Collectors.toMap(Novel::getId, n -> n));

        NovelRankingVO vo = new NovelRankingVO();
        List<NovelRankingVO.Item> items = new ArrayList<>();
        int rank = 1;
        for (ZSetEntry entry : entries) {
            Long novelId = Long.parseLong(entry.getMember());
            Novel novel = novelMap.get(novelId);
            if (novel == null) {
                continue;
            }
            NovelRankingVO.Item item = new NovelRankingVO.Item();
            item.setRank(rank++);
            item.setId(novelId);
            item.setName(novel.getName());
            item.setAuthorName(novel.getAuthorName());
            item.setChapterCount(novel.getChapterCount());
            item.setCollectCount(novel.getCollectCount());
            item.setAllWordCount(novel.getAllWordCount());
            item.setIsFinished(novel.getIsFinished());
            item.setIsHot(novel.getIsHot());
            item.setUrl(novel.getUrl());
            item.setScore(entry.getScore());
            items.add(item);
        }
        vo.setItems(items);

        log.info("访客端-{}获取完成：结果数={}", rankingName, items.size());
        return Result.success(vo);
    }
}
