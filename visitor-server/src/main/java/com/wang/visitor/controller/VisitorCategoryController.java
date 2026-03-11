package com.wang.visitor.controller;

import com.wang.common.result.Result;
import com.wang.visitor.service.VisitorCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 访客端分类控制器
 */
@Slf4j
@RestController
@RequestMapping("/visitor/category")
@Api(tags = "访客-分类浏览")
public class VisitorCategoryController {

    private final VisitorCategoryService visitorCategoryService;

    public VisitorCategoryController(VisitorCategoryService visitorCategoryService) {
        this.visitorCategoryService = visitorCategoryService;
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/list")
    @ApiOperation("获取所有分类")
    public Result getAllCategories() {
        log.info("获取所有分类");
        return visitorCategoryService.getAllCategories();
    }

    /**
     * 根据频道获取分类
     */
    @GetMapping("/list/{category}")
    @ApiOperation("根据频道获取分类")
    public Result getCategoriesByChannel(@PathVariable Integer category) {
        log.info("根据频道获取分类：category={}", category);
        return visitorCategoryService.getCategoriesByChannel(category);
    }

    /**
     * 获取热门分类
     */
    @GetMapping("/hot")
    @ApiOperation("获取热门分类")
    public Result getHotCategories() {
        log.info("获取热门分类");
        return visitorCategoryService.getHotCategories();
    }
}