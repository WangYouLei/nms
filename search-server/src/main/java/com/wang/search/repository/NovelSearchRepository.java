package com.wang.search.repository;

import com.wang.search.document.NovelDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 小说搜索 Repository
 */
public interface NovelSearchRepository extends ElasticsearchRepository<NovelDocument, Long> {
}
