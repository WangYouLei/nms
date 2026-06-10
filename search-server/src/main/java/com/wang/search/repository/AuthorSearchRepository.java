package com.wang.search.repository;

import com.wang.search.document.AuthorDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 作者搜索 Repository
 */
public interface AuthorSearchRepository extends ElasticsearchRepository<AuthorDocument, Long> {
}
