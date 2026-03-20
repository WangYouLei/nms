package com.wang.novel.controller;

import com.wang.common.result.Result;
import com.wang.novel.service.NovelChapterService;
import com.wang.pojo.dto.NovelChapterDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 小说章节控制器
 * 提供 author、manager、visitor 三个端口的章节相关接口
 */
@Slf4j
@RestController
@Api(tags = "小说章节管理")
@Validated
public class NovelChapterController {

    private final NovelChapterService novelChapterService;

    public NovelChapterController(NovelChapterService novelChapterService) {
        this.novelChapterService = novelChapterService;
    }

    // ==================== Common - 公共接口（三个端口共用） ====================

    @GetMapping("/common/chapter/list")
    @ApiOperation("[Common] 查询章节列表")
    public Result getChapterList(@RequestParam Integer novelId) {
        log.info("[Common] 查询章节列表：小说ID={}", novelId);
        return novelChapterService.getChapterList(novelId);
    }

    @GetMapping("/common/chapter/detail/{id}")
    @ApiOperation("[Common] 获取章节详情")
    public Result getChapterDetail(@PathVariable Integer id) {
        log.info("[Common] 获取章节详情：章节ID={}", id);
        return novelChapterService.getChapterDetail(id);
    }

    @GetMapping("/common/chapter/content/{id}")
    @ApiOperation("[Common] 获取章节内容")
    public Result getChapterContent(@PathVariable Integer id) {
        log.info("[Common] 获取章节内容：章节ID={}", id);
        return novelChapterService.getChapterContent(id);
    }

    // ==================== Author - 作者端接口 ====================

    @PostMapping("/author/chapter/upload")
    @ApiOperation("[Author] 上传新章节")
    public Result authorUploadChapter(
            @RequestParam Integer novelId,
            @RequestParam String title,
            @RequestParam("file") MultipartFile file) {
        log.info("[Author] 上传新章节：小说ID={}, 新章节标题={}", novelId, title);
        return novelChapterService.uploadChapter(novelId, title, file);
    }

    @DeleteMapping("/author/chapter/delete/{id}")
    @ApiOperation("[Author] 删除章节")
    public Result authorDeleteChapter(@PathVariable Integer id) {
        log.info("[Author] 删除章节：章节ID={}", id);
        return novelChapterService.deleteChapter(id);
    }

    @PutMapping("/author/chapter/update")
    @ApiOperation("[Author] 更新章节信息")
    public Result authorUpdateChapter(@RequestBody NovelChapterDTO chapterDTO) {
        log.info("[Author] 更新章节：章节ID={}", chapterDTO.getId());
        return novelChapterService.updateChapter(chapterDTO);
    }

}