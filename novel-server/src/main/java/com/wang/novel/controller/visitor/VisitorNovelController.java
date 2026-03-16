package com.wang.novel.controller.visitor;

import com.wang.common.result.Result;
import com.wang.novel.service.visitor.VisitorNovelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

/**
 * 访客端小说控制器
 */
@Slf4j
@RestController
@RequestMapping("/visitor/novel")
@Api(tags = "访客-小说浏览")
public class VisitorNovelController {

    private final VisitorNovelService visitorNovelService;

    public VisitorNovelController(VisitorNovelService visitorNovelService) {
        this.visitorNovelService = visitorNovelService;
    }

    /**
     * 分页查询小说列表
     */
    @GetMapping("/list")
    @ApiOperation("分页查询小说列表")
    public Result getNovelList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        log.info("查询小说列表：页码={}, 每页数量={}", pageNum, pageSize);
        return visitorNovelService.getNovelList(pageNum, pageSize, keyword);
    }

    /**
     * 分页查询热门小说
     * @param pageNum 页码，默认为1
     * @param pageSize 每页数量，默认为10
     * @param categoryId 分类ID（可选）
     * @return 分页热门小说列表
     */
    @GetMapping("/hot")
    @ApiOperation("分页查询热门小说")
    public Result getHotNovels(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer categoryId) {
        log.info("分页查询热门小说：页码={}, 每页数量={}, 分类ID={}", pageNum, pageSize, categoryId);
        return visitorNovelService.getHotNovels(pageNum, pageSize, categoryId);
    }

    /**
     * 获取小说详情
     */
    @GetMapping("/{novelId}")
    @ApiOperation("获取小说详情")
    public Result getNovelDetail(@PathVariable Integer novelId) {
        log.info("获取小说详情：ID={}", novelId);
        return visitorNovelService.getNovelDetail(novelId);
    }

    /**
     * 按分类查询小说
     * @param pageNum 页码，默认为1
     * @param pageSize 每页数量，默认为10
     * @param categoryId 分类ID
     * @return 分页小说列表
     */
    @GetMapping("/category/{categoryId}")
    @ApiOperation("按分类查询小说")
    public Result getNovelsByCategory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @PathVariable Integer categoryId) {
        log.info("按分类查询小说：分类ID={}", categoryId);
        return visitorNovelService.getNovelsByCategory(pageNum, pageSize, categoryId);
    }
}