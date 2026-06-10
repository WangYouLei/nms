package com.wang.search.controller;

import com.wang.common.result.Result;
import com.wang.search.service.DataSyncService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索内部控制器
 * 仅供微服务间 Feign 调用，不对外暴露
 */
@Slf4j
@RestController
@Api(tags = "搜索服务-内部接口")
public class SearchInternalController {

    private final DataSyncService dataSyncService;

    public SearchInternalController(DataSyncService dataSyncService) {
        this.dataSyncService = dataSyncService;
    }

    @PostMapping("/internal/sync/all")
    @ApiOperation("[Internal] 全量同步数据")
    public Result syncAll() {
        log.info("[Internal] 执行全量同步");
        return dataSyncService.syncAll();
    }

    @PostMapping("/internal/sync/novel/{novelId}")
    @ApiOperation("[Internal] 单条小说同步")
    public Result syncNovel(@PathVariable Long novelId) {
        log.info("[Internal] 同步小说：novelId={}", novelId);
        return dataSyncService.syncNovel(novelId);
    }

    @PostMapping("/internal/sync/author/{authorId}")
    @ApiOperation("[Internal] 单条作者同步")
    public Result syncAuthor(@PathVariable Long authorId) {
        log.info("[Internal] 同步作者：authorId={}", authorId);
        return dataSyncService.syncAuthor(authorId);
    }
}
