package com.wang.search.service.impl;

import com.wang.common.enums.BizCodeEnum;
import com.wang.common.feign.AuthorServiceFeign;
import com.wang.common.feign.NovelServiceFeign;
import com.wang.common.result.Result;
import com.wang.search.document.AuthorDocument;
import com.wang.search.document.NovelDocument;
import com.wang.search.repository.AuthorSearchRepository;
import com.wang.search.repository.NovelSearchRepository;
import com.wang.search.service.DataSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 数据同步服务实现类
 * 负责将 MySQL 数据同步到 Elasticsearch
 */
@Service
@Slf4j
public class DataSyncServiceImpl implements DataSyncService {

    private final NovelSearchRepository novelSearchRepository;
    private final AuthorSearchRepository authorSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final NovelServiceFeign novelServiceFeign;
    private final AuthorServiceFeign authorServiceFeign;

    public DataSyncServiceImpl(NovelSearchRepository novelSearchRepository,
                               AuthorSearchRepository authorSearchRepository,
                               ElasticsearchOperations elasticsearchOperations,
                               NovelServiceFeign novelServiceFeign,
                               AuthorServiceFeign authorServiceFeign) {
        this.novelSearchRepository = novelSearchRepository;
        this.authorSearchRepository = authorSearchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.novelServiceFeign = novelServiceFeign;
        this.authorServiceFeign = authorServiceFeign;
    }

    @Override
    public Result syncAll() {
        log.info("开始全量同步数据到ES...");
        long startTime = System.currentTimeMillis();

        int novelSuccess = 0;
        int novelFail = 0;
        int authorSuccess = 0;
        int authorFail = 0;

        // 确保索引存在
        try {
            IndexOperations novelIndexOps = elasticsearchOperations.indexOps(NovelDocument.class);
            if (!novelIndexOps.exists()) {
                novelIndexOps.createWithMapping();
                log.info("创建小说索引成功");
            }

            IndexOperations authorIndexOps = elasticsearchOperations.indexOps(AuthorDocument.class);
            if (!authorIndexOps.exists()) {
                authorIndexOps.createWithMapping();
                log.info("创建作者索引成功");
            }
        } catch (Exception e) {
            log.error("创建ES索引失败：{}", e.getMessage());
            return Result.error("创建ES索引失败：" + e.getMessage());
        }

        // 同步小说数据
        // 注意：全量同步需要从 novel-server 的内部接口拉取数据
        // 当前 Feign 接口只提供单条查询，后续可扩展批量接口
        // 此处先提供框架，实际批量拉取需要 novel-server 新增内部接口
        log.info("全量同步框架已就绪，请通过 novel-server 的批量接口拉取数据后调用 syncNovel 逐条同步");
        log.info("提示：可通过 POST /internal/sync/novel/{id} 和 POST /internal/sync/author/{id} 逐条同步");

        long costTime = System.currentTimeMillis() - startTime;
        log.info("全量同步完成：小说成功={}, 失败={}, 作者成功={}, 失败={}, 耗时={}ms",
                novelSuccess, novelFail, authorSuccess, authorFail, costTime);

        Map<String, Object> result = Map.of(
                "novelSuccess", novelSuccess,
                "novelFail", novelFail,
                "authorSuccess", authorSuccess,
                "authorFail", authorFail,
                "costTimeMs", costTime
        );
        return Result.success(result);
    }

    @Override
    public Result syncNovel(Long novelId) {
        if (novelId == null || novelId < 1) {
            return Result.error("小说ID无效");
        }

        try {
            // 通过 Feign 调用 novel-server 获取小说完整信息
            Result novelResult = novelServiceFeign.getNovelBasicInfo(novelId);
            if (novelResult.getCode() != BizCodeEnum.SUCCESS.getCode() || novelResult.getData() == null) {
                log.warn("获取小说信息失败：novelId={}", novelId);
                return Result.error("获取小说信息失败");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> info = (Map<String, Object>) novelResult.getData();

            NovelDocument document = new NovelDocument();
            document.setId(((Number) info.get("id")).longValue());
            document.setName((String) info.get("name"));
            document.setSubName((String) info.get("subName"));
            document.setUrl((String) info.get("url"));
            document.setTags((String) info.get("tags"));
            document.setIntroduction((String) info.get("introduction"));

            Object authorIdObj = info.get("authorId");
            document.setAuthorId(authorIdObj != null ? ((Number) authorIdObj).longValue() : null);

            document.setAuthorName((String) info.get("authorName"));
            document.setAuthorAvatar((String) info.get("authorAvatar"));

            Object authorRankObj = info.get("authorRank");
            document.setAuthorRank(authorRankObj != null ? ((Number) authorRankObj).intValue() : null);

            Object chapterCountObj = info.get("chapterCount");
            document.setChapterCount(chapterCountObj != null ? ((Number) chapterCountObj).intValue() : null);

            Object allWordCountObj = info.get("allWordCount");
            document.setAllWordCount(allWordCountObj != null ? ((Number) allWordCountObj).intValue() : null);

            Object collectCountObj = info.get("collectCount");
            document.setCollectCount(collectCountObj != null ? ((Number) collectCountObj).intValue() : null);

            document.setIsFinished(info.get("isFinished") != null && (Boolean) info.get("isFinished"));
            document.setIsHot(info.get("isHot") != null && (Boolean) info.get("isHot"));
            document.setIsDel(info.get("isDel") != null && (Boolean) info.get("isDel"));

            // updateTime 可能为 String（经 Feign JSON 序列化后）
            Object updateTimeObj = info.get("updateTime");
            if (updateTimeObj instanceof java.time.LocalDateTime) {
                document.setUpdateTime((java.time.LocalDateTime) updateTimeObj);
            } else if (updateTimeObj instanceof String) {
                document.setUpdateTime(java.time.LocalDateTime.parse((String) updateTimeObj,
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
            }

            // 分类信息（经 JSON 序列化后可能是 Integer 而非 Long，需安全转换）
            @SuppressWarnings("unchecked")
            List<Object> rawCategoryIds = (List<Object>) info.get("categoryIds");
            if (rawCategoryIds != null) {
                document.setCategoryIds(rawCategoryIds.stream()
                        .map(o -> ((Number) o).longValue())
                        .collect(java.util.stream.Collectors.toList()));
            }

            @SuppressWarnings("unchecked")
            List<String> categoryNames = (List<String>) info.get("categoryNames");
            document.setCategoryNames(categoryNames);

            @SuppressWarnings("unchecked")
            List<String> categoryNamesKeyword = (List<String>) info.get("categoryNamesKeyword");
            document.setCategoryNamesKeyword(categoryNamesKeyword);

            Object categoryTypeObj = info.get("categoryType");
            document.setCategoryType(categoryTypeObj != null ? ((Number) categoryTypeObj).intValue() : null);

            novelSearchRepository.save(document);
            log.info("同步小说到ES成功：novelId={}", novelId);
            return Result.success("同步成功");
        } catch (Exception e) {
            log.error("同步小说到ES失败：novelId={}, error={}", novelId, e.getMessage());
            return Result.error("同步失败：" + e.getMessage());
        }
    }

    @Override
    public Result syncAuthor(Long authorId) {
        if (authorId == null || authorId < 1) {
            return Result.error("作者ID无效");
        }

        try {
            // 通过 Feign 调用 author-server 获取作者基本信息
            Result authorResult = authorServiceFeign.getAuthorBasicInfo(authorId);
            if (authorResult.getCode() != BizCodeEnum.SUCCESS.getCode() || authorResult.getData() == null) {
                log.warn("获取作者基本信息失败：authorId={}", authorId);
                return Result.error("获取作者信息失败");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> basicInfo = (Map<String, Object>) authorResult.getData();

            AuthorDocument document = new AuthorDocument();
            document.setId(((Number) basicInfo.get("id")).longValue());
            document.setName((String) basicInfo.get("name"));
            document.setAvatar((String) basicInfo.get("avatar"));

            Object rankObj = basicInfo.get("rank");
            document.setRank(rankObj != null ? ((Number) rankObj).intValue() : null);

            document.setIntroduction((String) basicInfo.get("introduction"));

            Object novelCountObj = basicInfo.get("novelCount");
            document.setNovelCount(novelCountObj != null ? ((Number) novelCountObj).intValue() : null);

            document.setIsDel(basicInfo.get("isDel") != null && (Boolean) basicInfo.get("isDel"));

            authorSearchRepository.save(document);
            log.info("同步作者到ES成功：authorId={}", authorId);
            return Result.success("同步成功");
        } catch (Exception e) {
            log.error("同步作者到ES失败：authorId={}, error={}", authorId, e.getMessage());
            return Result.error("同步失败：" + e.getMessage());
        }
    }
}
