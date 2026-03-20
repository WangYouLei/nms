package com.wang.novel.controller;

import com.wang.common.result.Result;
import com.wang.novel.service.NovelCategoryService;
import com.wang.pojo.dto.NovelCategoryDTO;
import com.wang.pojo.dto.NovelCategoryRelationDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 小说分类控制器
 * 提供 author、manager、visitor 三个端口的分类相关接口
 */
@Slf4j
@RestController
@Api(tags = "小说分类管理")
@Validated
public class NovelCategoryController {

    private final NovelCategoryService novelCategoryService;

    public NovelCategoryController(NovelCategoryService novelCategoryService) {
        this.novelCategoryService = novelCategoryService;
    }

    // ==================== Common - 公共接口（三个端口共用） ====================

    @GetMapping("/common/category/list")
    @ApiOperation("[Common] 获取所有分类")
    public Result getAllCategories() {
        log.info("[Common] 获取所有分类");
        return novelCategoryService.getAllCategories();
    }

    @GetMapping("/common/category/channel/{category}")
    @ApiOperation("[Common] 根据频道获取分类")
    public Result getCategoriesByChannel(@PathVariable Integer category) {
        log.info("[Common] 根据频道获取分类：category={}", category);
        return novelCategoryService.getCategoriesByChannel(category);
    }

    @GetMapping("/common/category/hot")
    @ApiOperation("[Common] 获取热门分类")
    public Result getHotCategories() {
        log.info("[Common] 获取热门分类");
        return novelCategoryService.getHotCategories();
    }

    @GetMapping("/common/category/{id}")
    @ApiOperation("[Common] 根据ID查询分类")
    public Result getCategoryById(@PathVariable Integer id) {
        log.info("[Common] 根据ID查询分类：id={}", id);
        return novelCategoryService.getCategoryById(id);
    }

    @GetMapping("/common/category/relation/{novelId}")
    @ApiOperation("[Common] 获取小说的分类")
    public Result getNovelCategory(@PathVariable Integer novelId) {
        log.info("[Common] 获取小说分类：小说ID={}", novelId);
        return novelCategoryService.getNovelCategory(novelId);
    }

    // ==================== Manager - 管理端接口 ====================

    @PostMapping("/manager/category/add")
    @ApiOperation("[Manager] 添加分类")
    public Result addCategory(@RequestBody @Validated NovelCategoryDTO dto) {
        log.info("[Manager] 添加分类：type={}, category={}", dto.getType(), dto.getCategory());
        return novelCategoryService.addCategory(dto);
    }

    @PutMapping("/manager/category/update")
    @ApiOperation("[Manager] 修改分类")
    public Result updateCategory(@RequestBody @Validated NovelCategoryDTO dto) {
        log.info("[Manager] 修改分类：ID={}", dto.getId());
        return novelCategoryService.updateCategory(dto);
    }

    @DeleteMapping("/manager/category/delete/{id}")
    @ApiOperation("[Manager] 删除分类")
    public Result deleteCategory(@PathVariable Integer id) {
        log.info("[Manager] 删除分类：ID={}", id);
        return novelCategoryService.deleteCategory(id);
    }

    @GetMapping("/manager/category/page")
    @ApiOperation("[Manager] 分页查询分类")
    public Result getCategoryList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer category) {
        log.info("[Manager] 分页查询分类：页码={}, 每页数量={}", pageNum, pageSize);
        return novelCategoryService.getCategoryList(pageNum, pageSize, type, category);
    }

    @PostMapping("/manager/category/relation/set")
    @ApiOperation("[Manager] 设置小说分类")
    public Result setNovelCategory(@RequestBody NovelCategoryRelationDTO dto) {
        log.info("[Manager] 设置小说分类：小说ID={}, 分类ID={}", dto.getNovelId(), dto.getCategoryId());
        return novelCategoryService.setNovelCategory(dto);
    }
}