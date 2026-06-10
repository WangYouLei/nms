package com.wang.manager.task;

import com.wang.common.constants.CacheConstants;
import com.wang.common.service.CacheService;
import com.wang.manager.mapper.StatisticsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 排行榜全量同步定时任务
 * 每天凌晨3点从 MySQL 全量同步到 Redis ZSET，防止数据漂移
 */
@Slf4j
@Component
public class RankingSyncTask {

    private final StatisticsMapper statisticsMapper;
    private final CacheService cacheService;

    public RankingSyncTask(StatisticsMapper statisticsMapper, CacheService cacheService) {
        this.statisticsMapper = statisticsMapper;
        this.cacheService = cacheService;
    }

    /**
     * 每天凌晨3点执行全量同步
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void syncRankingData() {
        log.info("排行榜全量同步开始");
        long startTime = System.currentTimeMillis();

        try {
            syncNovelRankings();
            syncAuthorRankings();
        } catch (Exception e) {
            log.error("排行榜全量同步异常：{}", e.getMessage(), e);
        }

        long cost = System.currentTimeMillis() - startTime;
        log.info("排行榜全量同步完成，耗时：{}ms", cost);
    }

    /**
     * 同步小说相关排行榜
     */
    private void syncNovelRankings() {
        List<LinkedHashMap<String, Object>> novels = statisticsMapper.selectAllNovelsForRankingSync();
        log.info("同步小说排行榜数据：共{}条", novels.size());

        // 先删除旧数据
        cacheService.delete(CacheConstants.RANKING_NOVEL_COLLECT);
        cacheService.delete(CacheConstants.RANKING_NOVEL_ONGOING);
        cacheService.delete(CacheConstants.RANKING_NOVEL_LATEST);
        cacheService.delete(CacheConstants.RANKING_NOVEL_NEW);

        for (LinkedHashMap<String, Object> novel : novels) {
            Long id = ((Number) novel.get("id")).longValue();
            String memberId = String.valueOf(id);
            boolean isFinished = novel.get("isFinished") != null && ((Number) novel.get("isFinished")).intValue() == 1;

            // 收藏榜
            int collectCount = novel.get("collectCount") != null ? ((Number) novel.get("collectCount")).intValue() : 0;
            cacheService.zAdd(CacheConstants.RANKING_NOVEL_COLLECT, collectCount, memberId);

            // 连载榜（仅连载中）
            if (!isFinished) {
                int chapterCount = novel.get("chapterCount") != null ? ((Number) novel.get("chapterCount")).intValue() : 0;
                cacheService.zAdd(CacheConstants.RANKING_NOVEL_ONGOING, chapterCount, memberId);
            }

            // 最新更新榜
            Object updateTs = novel.get("updateTimestamp");
            if (updateTs != null) {
                cacheService.zAdd(CacheConstants.RANKING_NOVEL_LATEST, ((Number) updateTs).doubleValue(), memberId);
            }

            // 新书榜
            Object createTs = novel.get("createTimestamp");
            if (createTs != null) {
                cacheService.zAdd(CacheConstants.RANKING_NOVEL_NEW, ((Number) createTs).doubleValue(), memberId);
            }
        }

        log.info("小说排行榜同步完成");
    }

    /**
     * 同步作者相关排行榜
     */
    private void syncAuthorRankings() {
        List<LinkedHashMap<String, Object>> authors = statisticsMapper.selectAllAuthorsForRankingSync();
        log.info("同步作者排行榜数据：共{}条", authors.size());

        // 先删除旧数据
        cacheService.delete(CacheConstants.RANKING_AUTHOR_PRODUCTIVE);

        for (LinkedHashMap<String, Object> author : authors) {
            Long id = ((Number) author.get("id")).longValue();
            String memberId = String.valueOf(id);
            int novelCount = author.get("novelCount") != null ? ((Number) author.get("novelCount")).intValue() : 0;

            // 作者高产榜
            cacheService.zAdd(CacheConstants.RANKING_AUTHOR_PRODUCTIVE, novelCount, memberId);
        }

        log.info("作者排行榜同步完成");
    }
}
