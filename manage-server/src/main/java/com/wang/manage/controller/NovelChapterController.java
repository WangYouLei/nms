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
     * 分页查询章节列表
     * @param novelId 小说ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 章节列表
     */
    @GetMapping("list")
    @ApiOperation("查询章节列表")
    public Result getChapterList(
            @RequestParam Integer novelId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("接收查询章节列表请求：小说ID={}, 页码={}, 每页数量={}", novelId, pageNum, pageSize);
        return novelChapterService.getChapterList(novelId, pageNum, pageSize);
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
}