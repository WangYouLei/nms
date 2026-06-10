package com.wang.common.feign;

import com.wang.common.feign.fallback.SearchServiceFeignFallback;
import com.wang.common.result.Result;
import com.wang.pojo.dto.SearchDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 搜索服务 Feign 客户端
 * 供其他微服务调用 search-server 的接口
 * 配置 fallback 降级处理，避免 search-server 不可用时级联故障
 */
@FeignClient(name = "search-server", fallback = SearchServiceFeignFallback.class)
public interface SearchServiceFeign {

    /**
     * 搜索小说（ES全文检索）
     * @param dto 搜索条件
     * @return 分页搜索结果
     */
    @PostMapping("/common/novel/search")
    Result searchNovels(@RequestBody SearchDTO dto);

    /**
     * 搜索作者
     * @param dto 搜索条件
     * @return 分页搜索结果
     */
    @PostMapping("/common/author/search")
    Result searchAuthors(@RequestBody SearchDTO dto);

    /**
     * 搜索建议
     * @param prefix 前缀关键词
     * @return 建议列表
     */
    @GetMapping("/common/search/suggest")
    Result suggest(@RequestParam("prefix") String prefix);

    /**
     * 分类搜索（聚合统计）
     * @param categoryType 频道类型（可选：1男频/2女频）
     * @return 分类聚合结果
     */
    @GetMapping("/common/search/categories")
    Result searchCategories(@RequestParam(value = "categoryType", required = false) Integer categoryType);

    /**
     * 全量同步（内部接口）
     * @return 同步结果
     */
    @PostMapping("/internal/sync/all")
    Result syncAll();

    /**
     * 单条小说同步（内部接口）
     * @param novelId 小说ID
     * @return 同步结果
     */
    @PostMapping("/internal/sync/novel/{novelId}")
    Result syncNovel(@PathVariable("novelId") Long novelId);

    /**
     * 单条作者同步（内部接口）
     * @param authorId 作者ID
     * @return 同步结果
     */
    @PostMapping("/internal/sync/author/{authorId}")
    Result syncAuthor(@PathVariable("authorId") Long authorId);
}
