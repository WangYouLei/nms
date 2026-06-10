package com.wang.search.controller;

import com.wang.common.result.Result;
import com.wang.pojo.dto.SearchDTO;
import com.wang.search.service.SearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索控制器
 * 提供公共搜索接口（小说搜索、作者搜索、搜索建议、分类聚合）
 */
@Slf4j
@RestController
@Api(tags = "搜索服务")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/common/novel/search")
    @ApiOperation("[Common] ES搜索小说列表")
    public Result searchNovels(@RequestBody SearchDTO dto) {
        log.info("[Common] ES搜索小说：keyword={}, categoryId={}, categoryType={}, tag={}",
                dto.getKeyword(), dto.getCategoryId(), dto.getCategoryType(), dto.getTag());
        return searchService.searchNovels(dto);
    }

    @PostMapping("/common/author/search")
    @ApiOperation("[Common] ES搜索作者列表")
    public Result searchAuthors(@RequestBody SearchDTO dto) {
        log.info("[Common] ES搜索作者：keyword={}", dto.getKeyword());
        return searchService.searchAuthors(dto);
    }

    @GetMapping("/common/search/suggest")
    @ApiOperation("[Common] 搜索建议（自动补全）")
    public Result suggest(@RequestParam String prefix) {
        log.info("[Common] 搜索建议：prefix={}", prefix);
        return searchService.suggest(prefix);
    }

    @GetMapping("/common/search/categories")
    @ApiOperation("[Common] 分类搜索（聚合统计）")
    public Result searchCategories(@RequestParam(required = false) Integer categoryType) {
        log.info("[Common] 分类搜索：categoryType={}", categoryType);
        return searchService.searchCategories(categoryType);
    }
}
