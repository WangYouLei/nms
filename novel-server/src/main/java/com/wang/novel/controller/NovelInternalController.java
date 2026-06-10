package com.wang.novel.controller;

import com.wang.common.result.Result;
import com.wang.novel.service.NovelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 小说内部接口控制器
 * 供其他微服务通过 Feign 调用，不对外暴露给前端
 */
@Slf4j
@RestController
@RequestMapping("/internal/novel")
public class NovelInternalController {

    private final NovelService novelService;

    public NovelInternalController(NovelService novelService) {
        this.novelService = novelService;
    }

    /**
     * 获取小说的作者ID
     * @param novelId 小说ID
     */
    @GetMapping("/authorId/{novelId}")
    public Result getNovelAuthorId(@PathVariable Long novelId) {
        log.info("[内部调用] 获取小说作者ID：novelId={}", novelId);
        return novelService.getNovelAuthorId(novelId);
    }

    /**
     * 批量获取小说的作者ID
     * @param novelIds 小说ID列表
     */
    @PostMapping("/batch-author-ids")
    public Result batchGetNovelAuthorIds(@RequestBody List<Long> novelIds) {
        log.info("[内部调用] 批量获取小说作者ID：count={}", novelIds.size());
        return novelService.batchGetNovelAuthorIds(novelIds);
    }

    /**
     * 获取小说基本信息（用于收藏时填充冗余字段）
     * @param novelId 小说ID
     */
    @GetMapping("/basic/{novelId}")
    public Result getNovelBasicInfo(@PathVariable Long novelId) {
        log.info("[内部调用] 获取小说基本信息：novelId={}", novelId);
        return novelService.getNovelBasicInfo(novelId);
    }
}
