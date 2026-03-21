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

    //TODO后期弄成es搜索
    @PostMapping("/common/novel/search")
    @ApiOperation("[Common] 搜索小说列表")
    public Result searchNovels(@RequestBody NovelSearchDTO dto) {
        log.info("[Common] 搜索小说：{}", dto);
        return novelService.searchNovels(dto);
    }

    // ==================== Author - 作者端接口 ====================

    @PostMapping("/author/novel/add")
    @ApiOperation("[Author] 新增小说")
    public Result addNovel(@RequestBody NovelDTO novel) {
        log.info("[Author] 新增小说：{}", novel.getName());
        return novelService.addNovel(novel);
    }

    @DeleteMapping("/author/novel/delete/{id}")
    @ApiOperation("[Author] 删除小说(逻辑删除)")
    public Result deleteNovel(@PathVariable Integer id) {
        log.info("[Author] 删除小说（逻辑删除）：ID={}", id);
        return novelService.deleteNovel(id);
    }

    @PutMapping("/author/novel/update")
    @ApiOperation("[Author] 修改小说信息")
    public Result updateNovel(@RequestBody NovelDTO novelDTO) {
        log.info("[Author] 修改小说：ID={}", novelDTO.getId());
        return novelService.updateNovel(novelDTO);
    }

    // ==================== Manager - 管理端接口 ====================

    @DeleteMapping("/manager/novel/delete/{id}")
    @ApiOperation("[Manager] 删除小说")
    public Result managerDeleteNovel(@PathVariable Integer id) {
        log.info("[Manager] 删除小说：ID={}", id);
        return novelService.deleteNovel(id);
    }

    // ==================== Visitor - 访客端接口 ====================

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