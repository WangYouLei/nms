package com.wang.manage.controller;

import com.wang.common.result.Result;
import com.wang.manage.service.NovelChapterService;
import com.wang.pojo.dto.NovelChapterDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 小说章节管理控制器
 */
@Slf4j
@RestController
@Api(tags = "小说章节管理")
@RequestMapping("/manager/chapter")
@Validated
public class NovelChapterController {

    private final NovelChapterService novelChapterService;

    public NovelChapterController(NovelChapterService novelChapterService) {
        this.novelChapterService = novelChapterService;
    }

    /**
     * 上传章节
     * @param novelId 小说ID
     * @param title 章节标题
     * @param file 章节文件(.md)
     * @return 操作结果
     */
    @PostMapping("upload")
    @ApiOperation("上传章节")
    public Result uploadChapter(
            @RequestParam Integer novelId,
            @RequestParam String title,
            @RequestParam("file") MultipartFile file) {
        log.info("接收上传章节请求：小说ID={}, 章节标题={}", novelId, title);
        return novelChapterService.uploadChapter(novelId, title, file);
    }

    /**
     * 删除章节
     * @param id 章节ID
     * @return 操作结果
     */
    @DeleteMapping("delete/{id}")
    @ApiOperation("删除章节")
    public Result deleteChapter(@PathVariable Integer id) {
        log.info("接收删除章节请求：章节ID={}", id);
        return novelChapterService.deleteChapter(id);
    }

    /**
     * 更新章节信息
     * @param chapterDTO 章节信息
     * @return 操作结果
     */
    @PutMapping("update")
    @ApiOperation("更新章节信息")
    public Result updateChapter(@RequestBody NovelChapterDTO chapterDTO) {
        log.info("接收更新章节请求：章节ID={}", chapterDTO.getId());
        return novelChapterService.updateChapter(chapterDTO);
    }

    /**
     * 查询小说的所有章节列表
     * @param novelId 小说ID
     * @return 章节列表
     */
    @GetMapping("list")
    @ApiOperation("查询章节列表")
    public Result getChapterList(@RequestParam Integer novelId) {
        log.info("接收查询章节列表请求：小说ID={}", novelId);
        return novelChapterService.getChapterList(novelId);
    }

    /**
     * 获取章节详情
     * @param id 章节ID
     * @return 章节详情
     */
    @GetMapping("detail/{id}")
    @ApiOperation("获取章节详情")
    public Result getChapterDetail(@PathVariable Integer id) {
        log.info("接收获取章节详情请求：章节ID={}", id);
        return novelChapterService.getChapterDetail(id);
    }

    /**
     * 保存章节内容（直接保存内容字符串）
     * @param novelId 小说ID
     * @param title 章节标题
     * @param content 章节内容
     * @return 操作结果
     */
    @PostMapping("save")
    @ApiOperation("保存章节内容")
    public Result saveChapterContent(
            @RequestParam Integer novelId,
            @RequestParam String title,
            @RequestParam String content) {
        log.info("接收保存章节内容请求：小说ID={}, 章节标题={}", novelId, title);
        return novelChapterService.saveChapterContent(novelId, title, content);
    }

    /**
     * 获取章节内容（包含文件内容）
     * @param id 章节ID
     * @return 章节内容
     */
    @GetMapping("content/{id}")
    @ApiOperation("获取章节内容")
    public Result getChapterContent(@PathVariable Integer id) {
        log.info("接收获取章节内容请求：章节ID={}", id);
        return novelChapterService.getChapterContent(id);
    }
}