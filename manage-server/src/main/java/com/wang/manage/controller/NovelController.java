package com.wang.manage.controller;

import com.wang.common.result.Result;
import com.wang.manage.service.NovelService;
import com.wang.pojo.dto.NovelDTO;
import com.wang.pojo.entity.Novel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;

/**
 * 小说管理控制器
 */
@Slf4j
@RestController
@Api(tags = "小说管理")
@RequestMapping("/novel")
@Validated
public class NovelController {

    @Autowired
    private NovelService novelService;

    /**
     * 新增小说
     * @param novel 小说信息
     * @return 操作结果
     */
    @PostMapping("add")
    @ApiOperation("新增小说")
    //@Valid 注解是用来触发Novel的校验,确保非空字段不为空等逻辑，否则抛出异常
    //这里不用这个注解，因为使用的是DTO类
    public Result addNovel(/*@Valid */@RequestBody NovelDTO novel) {
        log.info("接收新增小说请求：{}", novel);
        return novelService.addNovel(novel);
    }

    /**
     * 根据ID删除小说
     * @param id 小说ID
     * @return 操作结果
     */
    @DeleteMapping("delete/{id}")
    @ApiOperation("删除小说")
    public Result deleteNovel(@PathVariable Integer id) {
        log.info("接收删除小说请求：ID={}", id);
        return novelService.deleteNovel(id);
    }
    
    /**
     * 分页查询当前登录作者的小说列表
     * @param pageNum 页码，默认为1
     * @param pageSize 每页数量，默认为10
     * @return 分页查询结果
     */
    @GetMapping("list")
    @ApiOperation("分页查询小说列表")
    public Result getNovelList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("接收分页查询小说列表请求：页码={}, 每页数量={}", pageNum, pageSize);
        return novelService.getNovelList(pageNum, pageSize);
    }
    
    /**
     * 根据小说名称或副名称进行模糊查询
     * @param name 小说名称（可选）
     * @param subName 小说副名称（可选）
     * @param pageNum 页码，默认为1
     * @param pageSize 每页数量，默认为10
     * @return 分页查询结果
     */
    @GetMapping("search")
    @ApiOperation("模糊查询小说")
    public Result searchNovels(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String subName,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("接收模糊查询小说请求：名称={}, 副名称={}, 页码={}, 每页数量={}", 
                 name, subName, pageNum, pageSize);
        return novelService.searchNovels(name, subName, pageNum, pageSize);
    }
    
    /**
     * 修改小说信息
     * @param novel 小说信息
     * @return 操作结果
     */
    @PutMapping("update")
    @ApiOperation("修改小说")
    public Result updateNovel(@Valid @RequestBody Novel novel) {
        log.info("接收修改小说请求：ID={}, 名称={}", novel.getId(), novel.getName());
        return novelService.updateNovel(novel);
    }
}
