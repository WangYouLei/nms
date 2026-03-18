package com.wang.novel.controller;

import com.wang.common.result.Result;
import com.wang.novel.service.NovelService;
import com.wang.pojo.dto.NovelDTO;
import com.wang.pojo.dto.NovelSearchDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 小说控制器
 * 提供 author、manager、visitor 三个端口的小说相关接口
 */
@Slf4j
@RestController
@Api(tags = "小说管理")
public class NovelController {

    private final NovelService novelService;

    public NovelController(NovelService novelService) {
        this.novelService = novelService;
    }

    // ==================== Common - 公共接口（三个端口共用） ====================

    @GetMapping("/common/novel/{novelId}")
    @ApiOperation("[Common] 获取小说详情")
    public Result getNovelDetail(@PathVariable Integer novelId) {
        log.info("[Common] 获取小说详情：ID={}", novelId);
        return novelService.getNovelDetail(novelId);
    }

    // ==================== Author - 作者端接口 ====================

    @PostMapping("/author/novel/add")
    @ApiOperation("[Author] 新增小说")
    public Result addNovel(@RequestBody NovelDTO novel) {
        log.info("[Author] 新增小说：{}", novel.getName());
        return novelService.addNovel(novel);
    }

    @DeleteMapping("/author/novel/delete/{id}")
    @ApiOperation("[Author] 删除小说")
    public Result deleteNovel(@PathVariable Integer id) {
        log.info("[Author] 删除小说：ID={}", id);
        return novelService.deleteNovel(id);
    }

    @GetMapping("/author/novel/list")
    @ApiOperation("[Author] 查询我的小说列表")
    public Result getMyNovelList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("[Author] 查询小说列表：页码={}, 每页数量={}", pageNum, pageSize);
        return novelService.getNovelList(pageNum, pageSize);
    }

    @PostMapping("/author/novel/search")
    @ApiOperation("[Author] 搜索我的小说")
    public Result searchMyNovels(@RequestBody NovelSearchDTO dto) {
        log.info("[Author] 搜索小说：{}", dto);
        return novelService.searchNovels(dto);
    }

    @PutMapping("/author/novel/update")
    @ApiOperation("[Author] 修改小说信息")
    public Result updateNovel(@RequestBody NovelDTO novelDTO) {
        log.info("[Author] 修改小说：ID={}", novelDTO.getId());
        return novelService.updateNovel(novelDTO);
    }

    // ==================== Manager - 管理端接口 ====================

    @PostMapping("/manager/novel/add")
    @ApiOperation("[Manager] 新增小说")
    public Result managerAddNovel(@RequestBody NovelDTO novel) {
        log.info("[Manager] 新增小说：{}", novel.getName());
        return novelService.addNovel(novel);
    }

    @DeleteMapping("/manager/novel/delete/{id}")
    @ApiOperation("[Manager] 删除小说")
    public Result managerDeleteNovel(@PathVariable Integer id) {
        log.info("[Manager] 删除小说：ID={}", id);
        return novelService.deleteNovel(id);
    }

    @PutMapping("/manager/novel/update")
    @ApiOperation("[Manager] 修改小说")
    public Result managerUpdateNovel(@RequestBody NovelDTO novelDTO) {
        log.info("[Manager] 修改小说：ID={}", novelDTO.getId());
        return novelService.updateNovel(novelDTO);
    }

    @GetMapping("/manager/novel/list")
    @ApiOperation("[Manager] 查询小说列表")
    public Result managerGetNovelList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("[Manager] 查询小说列表：页码={}, 每页数量={}", pageNum, pageSize);
        return novelService.getNovelList(pageNum, pageSize);
    }

    @PostMapping("/manager/novel/search")
    @ApiOperation("[Manager] 搜索小说")
    public Result managerSearchNovels(@RequestBody NovelSearchDTO dto) {
        log.info("[Manager] 搜索小说：{}", dto);
        return novelService.searchNovels(dto);
    }

    // ==================== Visitor - 访客端接口 ====================

    @GetMapping("/visitor/novel/list")
    @ApiOperation("[Visitor] 分页查询小说列表")
    public Result visitorGetNovelList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        log.info("[Visitor] 查询小说列表：页码={}, 每页数量={}", pageNum, pageSize);
        return novelService.getVisitorNovelList(pageNum, pageSize, keyword);
    }

    @GetMapping("/visitor/novel/hot")
    @ApiOperation("[Visitor] 分页查询热门小说")
    public Result getHotNovels(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer categoryId) {
        log.info("[Visitor] 分页查询热门小说：页码={}, 每页数量={}, 分类ID={}", pageNum, pageSize, categoryId);
        return novelService.getHotNovels(pageNum, pageSize, categoryId);
    }

    @GetMapping("/visitor/novel/category/{categoryId}")
    @ApiOperation("[Visitor] 按分类查询小说")
    public Result getNovelsByCategory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @PathVariable Integer categoryId) {
        log.info("[Visitor] 按分类查询小说：分类ID={}", categoryId);
        return novelService.getNovelsByCategory(pageNum, pageSize, categoryId);
    }
}