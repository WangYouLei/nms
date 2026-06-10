package com.wang.common.feign.fallback;

import com.wang.common.feign.SearchServiceFeign;
import com.wang.common.result.Result;
import com.wang.pojo.dto.SearchDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 搜索服务 Feign 降级处理
 * 当 search-server 不可用时，返回默认值而非抛出异常，避免级联故障
 */
@Slf4j
@Component
public class SearchServiceFeignFallback implements SearchServiceFeign {

    @Override
    public Result searchNovels(SearchDTO dto) {
        log.warn("[Feign降级] ES搜索小说失败，将降级到MySQL搜索：keyword={}", dto.getKeyword());
        return Result.error("搜索服务不可用，请稍后重试");
    }

    @Override
    public Result searchAuthors(SearchDTO dto) {
        log.warn("[Feign降级] ES搜索作者失败：keyword={}", dto.getKeyword());
        return Result.error("搜索服务不可用，请稍后重试");
    }

    @Override
    public Result suggest(@RequestParam("prefix") String prefix) {
        log.warn("[Feign降级] 搜索建议失败：prefix={}", prefix);
        return Result.error("搜索服务不可用");
    }

    @Override
    public Result searchCategories(@RequestParam(value = "categoryType", required = false) Integer categoryType) {
        log.warn("[Feign降级] 分类搜索失败：categoryType={}", categoryType);
        return Result.error("搜索服务不可用");
    }

    @Override
    public Result syncAll() {
        log.warn("[Feign降级] 全量同步失败");
        return Result.error("搜索服务不可用");
    }

    @Override
    public Result syncNovel(Long novelId) {
        log.warn("[Feign降级] 单条小说同步失败：novelId={}", novelId);
        return Result.error("搜索服务不可用");
    }

    @Override
    public Result syncAuthor(Long authorId) {
        log.warn("[Feign降级] 单条作者同步失败：authorId={}", authorId);
        return Result.error("搜索服务不可用");
    }
}
