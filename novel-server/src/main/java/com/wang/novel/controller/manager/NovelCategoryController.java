package com.wang.novel.controller.manager;

import com.wang.common.result.Result;
import com.wang.novel.service.manager.NovelCategoryService;
import com.wang.pojo.dto.NovelCategoryDTO;
import com.wang.pojo.dto.NovelCategoryRelationDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 小说分类管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/manager/category")
@Api(tags = "小说分类管理")
@Validated
public class NovelCategoryController {

    private final NovelCategoryService novelCategoryService;
    @Autowired
    public NovelCategoryController(NovelCategoryService novelCategoryService) {
        this.novelCategoryService = novelCategoryService;
    }

    @PostMapping("/add")
    @ApiOperation("添加分类")
    public Result addCategory(@RequestBody @Validated NovelCategoryDTO dto) {
        return novelCategoryService.addCategory(dto);
    }

    @PutMapping("/update")
    @ApiOperation("修改分类")
    public Result updateCategory(@RequestBody @Validated NovelCategoryDTO dto) {
        return novelCategoryService.updateCategory(dto);
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除分类")
    public Result deleteCategory(@PathVariable Integer id) {
        return novelCategoryService.deleteCategory(id);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询分类")
    public Result getCategoryById(@PathVariable Integer id) {
        return novelCategoryService.getCategoryById(id);
    }

    @GetMapping("/list")
    @ApiOperation("查询所有分类")
    public Result getAllCategories() {
        return novelCategoryService.getAllCategories();
    }

    @GetMapping("/listByCategory/{category}")
    @ApiOperation("根据频道查询分类")
    public Result getCategoriesByCategory(@PathVariable Integer category) {
        return novelCategoryService.getCategoriesByCategory(category);
    }

    @GetMapping("/hot")
    @ApiOperation("查询热门分类")
    public Result getHotCategories() {
        return novelCategoryService.getHotCategories();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询分类")
    public Result getCategoryList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer category) {
        return novelCategoryService.getCategoryList(pageNum, pageSize, type, category);
    }

    // ==================== 小说分类关联 ====================

    @PostMapping("/relation/set")
    @ApiOperation("设置小说分类")
    public Result setNovelCategory(@RequestBody NovelCategoryRelationDTO dto) {
        return novelCategoryService.setNovelCategory(dto);
    }

    @GetMapping("/relation/{novelId}")
    @ApiOperation("获取小说的分类")
    public Result getNovelCategory(@PathVariable Integer novelId) {
        return novelCategoryService.getNovelCategory(novelId);
    }
}