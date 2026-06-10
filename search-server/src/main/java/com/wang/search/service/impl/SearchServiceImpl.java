package com.wang.search.service.impl;

import com.wang.common.enums.BizCodeEnum;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.pojo.dto.SearchDTO;
import com.wang.search.document.AuthorDocument;
import com.wang.search.document.NovelDocument;
import com.wang.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;

/**
 * 搜索服务实现类
 * 基于 Elasticsearch 实现全文检索、分类聚合、搜索建议
 */
@Service
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 10;

    public SearchServiceImpl(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public Result searchNovels(SearchDTO dto) {
        long startTime = System.currentTimeMillis();

        // 参数校验
        Integer pageNum = dto.getPageNum();
        Integer pageSize = dto.getPageSize();
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1 || pageSize > MAX_PAGE_SIZE) pageSize = DEFAULT_PAGE_SIZE;

        // 构建 BoolQuery
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        // 过滤已删除的
        boolBuilder.filter(f -> f.term(t -> t.field("isDel").value(FieldValue.of(false))));

        // 关键词全文搜索（搜索 name/subName/tags/introduction/authorName/categoryNames）
        if (StringUtils.hasText(dto.getKeyword())) {
            boolBuilder.must(m -> m.multiMatch(MultiMatchQuery.of(mm -> mm
                    .query(dto.getKeyword())
                    .fields("name^3", "subName^2", "tags^2", "authorName^2", "categoryNames^1.5", "introduction^1")
            )));
        }

        // 精确条件筛选
        if (StringUtils.hasText(dto.getName())) {
            boolBuilder.must(m -> m.matchPhrase(mp -> mp.field("name").query(dto.getName())));
        }
        if (StringUtils.hasText(dto.getSubName())) {
            boolBuilder.must(m -> m.matchPhrase(mp -> mp.field("subName").query(dto.getSubName())));
        }
        if (dto.getAuthorId() != null) {
            boolBuilder.filter(f -> f.term(t -> t.field("authorId").value(FieldValue.of(dto.getAuthorId()))));
        }
        if (dto.getIsHot() != null) {
            boolBuilder.filter(f -> f.term(t -> t.field("isHot").value(FieldValue.of(dto.getIsHot()))));
        }
        if (dto.getIsFinished() != null) {
            boolBuilder.filter(f -> f.term(t -> t.field("isFinished").value(FieldValue.of(dto.getIsFinished()))));
        }
        if (dto.getCategoryId() != null) {
            boolBuilder.filter(f -> f.term(t -> t.field("categoryIds").value(FieldValue.of(dto.getCategoryId()))));
        }
        if (dto.getCategoryType() != null) {
            boolBuilder.filter(f -> f.term(t -> t.field("categoryType").value(FieldValue.of(dto.getCategoryType()))));
        }
        if (StringUtils.hasText(dto.getTag())) {
            boolBuilder.must(m -> m.matchPhrase(mp -> mp.field("tags").query(dto.getTag())));
        }

        // 排序
        String sortBy = dto.getSortBy();
        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(q -> q.bool(boolBuilder.build()))
                .withPageable(org.springframework.data.domain.PageRequest.of(pageNum - 1, pageSize));

        if (!StringUtils.hasText(dto.getKeyword())) {
            // 无关键词时按指定字段排序
            if ("collect".equals(sortBy)) {
                queryBuilder.withSort(s -> s.field(f -> f.field("collectCount").order(SortOrder.Desc)));
            } else if ("word".equals(sortBy)) {
                queryBuilder.withSort(s -> s.field(f -> f.field("allWordCount").order(SortOrder.Desc)));
            } else {
                queryBuilder.withSort(s -> s.field(f -> f.field("updateTime").order(SortOrder.Desc)));
            }
        }
        // 有关键词时 ES 默认按相关度排序

        // 高亮
        if (StringUtils.hasText(dto.getKeyword())) {
            HighlightFieldParameters fieldParams = HighlightFieldParameters.builder()
                    .withPreTags("<em class='highlight'>")
                    .withPostTags("</em>")
                    .build();
            Highlight highlight = new Highlight(List.of(
                    new HighlightField("name", fieldParams),
                    new HighlightField("subName", fieldParams),
                    new HighlightField("tags", fieldParams),
                    new HighlightField("authorName", fieldParams),
                    new HighlightField("introduction", fieldParams)
            ));
            queryBuilder.withHighlightQuery(new HighlightQuery(highlight, NovelDocument.class));
        }

        // 执行查询
        SearchHits<NovelDocument> searchHits = elasticsearchOperations.search(
                queryBuilder.build(), NovelDocument.class, IndexCoordinates.of("novel_index"));

        // 转换结果
        List<Map<String, Object>> voList = searchHits.getSearchHits().stream()
                .map(hit -> {
                    Map<String, Object> map = new HashMap<>();
                    NovelDocument doc = hit.getContent();
                    map.put("id", doc.getId());
                    map.put("name", doc.getName());
                    map.put("subName", doc.getSubName());
                    map.put("tags", doc.getTags());
                    map.put("introduction", doc.getIntroduction());
                    map.put("authorName", doc.getAuthorName());
                    map.put("authorId", doc.getAuthorId());
                    map.put("authorAvatar", doc.getAuthorAvatar());
                    map.put("authorRank", doc.getAuthorRank());
                    map.put("url", doc.getUrl());
                    map.put("chapterCount", doc.getChapterCount());
                    map.put("allWordCount", doc.getAllWordCount());
                    map.put("collectCount", doc.getCollectCount());
                    map.put("isFinished", doc.getIsFinished());
                    map.put("isHot", doc.getIsHot());
                    map.put("updateTime", doc.getUpdateTime());
                    map.put("categoryIds", doc.getCategoryIds());
                    map.put("categoryNames", doc.getCategoryNames());
                    map.put("categoryType", doc.getCategoryType());
                    // 高亮字段
                    if (StringUtils.hasText(dto.getKeyword()) && hit.getHighlightFields() != null) {
                        Map<String, List<String>> highlightFields = new HashMap<>();
                        hit.getHighlightFields().forEach((field, highlights) -> {
                            if (!highlights.isEmpty()) {
                                highlightFields.put(field, highlights);
                            }
                        });
                        if (!highlightFields.isEmpty()) {
                            map.put("highlights", highlightFields);
                        }
                    }
                    return map;
                })
                .collect(Collectors.toList());

        long total = searchHits.getTotalHits();
        PageResult<Map<String, Object>> pageResult = PageResult.build(pageNum, pageSize, total, voList);

        long costTime = System.currentTimeMillis() - startTime;
        log.info("ES搜索小说完成：keyword={}, 命中{}条, 耗时{}ms", dto.getKeyword(), total, costTime);
        return Result.success(pageResult);
    }

    @Override
    public Result searchAuthors(SearchDTO dto) {
        long startTime = System.currentTimeMillis();

        Integer pageNum = dto.getPageNum();
        Integer pageSize = dto.getPageSize();
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1 || pageSize > MAX_PAGE_SIZE) pageSize = DEFAULT_PAGE_SIZE;

        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        // 过滤已删除的
        boolBuilder.filter(f -> f.term(t -> t.field("isDel").value(FieldValue.of(false))));

        // 关键词全文搜索
        if (StringUtils.hasText(dto.getKeyword())) {
            boolBuilder.must(m -> m.multiMatch(MultiMatchQuery.of(mm -> mm
                    .query(dto.getKeyword())
                    .fields("name^3", "introduction^1")
            )));
        }

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(boolBuilder.build()))
                .withPageable(org.springframework.data.domain.PageRequest.of(pageNum - 1, pageSize))
                .build();

        SearchHits<AuthorDocument> searchHits = elasticsearchOperations.search(
                query, AuthorDocument.class, IndexCoordinates.of("author_index"));

        List<Map<String, Object>> voList = searchHits.getSearchHits().stream()
                .map(hit -> {
                    Map<String, Object> map = new HashMap<>();
                    AuthorDocument doc = hit.getContent();
                    map.put("id", doc.getId());
                    map.put("name", doc.getName());
                    map.put("introduction", doc.getIntroduction());
                    map.put("avatar", doc.getAvatar());
                    map.put("rank", doc.getRank());
                    map.put("novelCount", doc.getNovelCount());
                    return map;
                })
                .collect(Collectors.toList());

        long total = searchHits.getTotalHits();
        PageResult<Map<String, Object>> pageResult = PageResult.build(pageNum, pageSize, total, voList);

        long costTime = System.currentTimeMillis() - startTime;
        log.info("ES搜索作者完成：keyword={}, 命中{}条, 耗时{}ms", dto.getKeyword(), total, costTime);
        return Result.success(pageResult);
    }

    @Override
    public Result suggest(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return Result.success(List.of());
        }

        // 小说名称前缀建议
        NativeQuery novelQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .filter(f -> f.term(t -> t.field("isDel").value(FieldValue.of(false))))
                        .must(m -> m.prefix(p -> p.field("name").value(prefix)))
                ))
                .withPageable(org.springframework.data.domain.PageRequest.of(0, 5))
                .build();

        SearchHits<NovelDocument> novelHits = elasticsearchOperations.search(
                novelQuery, NovelDocument.class, IndexCoordinates.of("novel_index"));

        List<String> novelSuggestions = novelHits.getSearchHits().stream()
                .map(hit -> hit.getContent().getName())
                .collect(Collectors.toList());

        // 作者名称前缀建议
        NativeQuery authorQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .filter(f -> f.term(t -> t.field("isDel").value(FieldValue.of(false))))
                        .must(m -> m.prefix(p -> p.field("name").value(prefix)))
                ))
                .withPageable(org.springframework.data.domain.PageRequest.of(0, 5))
                .build();

        SearchHits<AuthorDocument> authorHits = elasticsearchOperations.search(
                authorQuery, AuthorDocument.class, IndexCoordinates.of("author_index"));

        List<String> authorSuggestions = authorHits.getSearchHits().stream()
                .map(hit -> hit.getContent().getName())
                .collect(Collectors.toList());

        Map<String, List<String>> suggestions = new HashMap<>();
        suggestions.put("novels", novelSuggestions);
        suggestions.put("authors", authorSuggestions);

        return Result.success(suggestions);
    }

    @Override
    public Result searchCategories(Integer categoryType) {
        // 使用聚合查询按 categoryNames 分组统计小说数量
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
        boolBuilder.filter(f -> f.term(t -> t.field("isDel").value(FieldValue.of(false))));

        if (categoryType != null) {
            boolBuilder.filter(f -> f.term(t -> t.field("categoryType").value(FieldValue.of(categoryType))));
        }

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(boolBuilder.build()))
                .withAggregation("category_aggregation", Aggregation.of(a -> a
                        .terms(TermsAggregation.of(t -> t
                                .field("categoryNamesKeyword")
                                .size(100)
                        ))
                ))
                .withMaxResults(0) // 只需要聚合结果，不需要文档
                .build();

        SearchHits<NovelDocument> searchHits = elasticsearchOperations.search(
                query, NovelDocument.class, IndexCoordinates.of("novel_index"));

        // 提取聚合结果
        List<Map<String, Object>> categories = new ArrayList<>();
        var aggregations = searchHits.getAggregations();
        if (aggregations != null) {
            @SuppressWarnings("unchecked")
            List<ElasticsearchAggregation> aggList = (List<ElasticsearchAggregation>) aggregations.aggregations();
            for (ElasticsearchAggregation esAgg : aggList) {
                var buckets = esAgg.aggregation().getAggregate().sterms().buckets().array();
                for (var bucket : buckets) {
                    Map<String, Object> cat = new HashMap<>();
                    cat.put("name", bucket.key().stringValue());
                    cat.put("count", bucket.docCount());
                    categories.add(cat);
                }
            }
        }

        return Result.success(categories);
    }
}
