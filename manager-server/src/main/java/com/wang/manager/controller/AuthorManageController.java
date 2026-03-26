package com.wang.manager.controller;

import com.wang.common.result.Result;
import com.wang.manager.service.AuthorManageService;
import com.wang.pojo.dto.AuthorQueryDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Api(tags = "作者管理")
@RequestMapping("/manager/author")
public class AuthorManageController {

    private final AuthorManageService authorManageService;

    @Autowired
    public AuthorManageController(AuthorManageService authorManageService) {
        this.authorManageService = authorManageService;
    }

    @GetMapping("/page")
    @ApiOperation("分页查询作者信息")
    public Result getAuthorPage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("分页查询作者请求：页码={}, 每页数量={}", pageNum, pageSize);
        return authorManageService.getAuthorPage(pageNum, pageSize);
    }

    @GetMapping("/list")
    @ApiOperation("多条件查询作者（支持id、姓名、账号、等级，条件可为空）")
    public Result getAuthorList(AuthorQueryDTO queryDTO) {
        log.info("多条件查询作者请求：queryDTO={}", queryDTO);
        return authorManageService.getAuthorList(queryDTO);
    }

    @GetMapping("/info/{id}")
    @ApiOperation("获取作者详情")
    public Result getAuthorInfo(@PathVariable Integer id) {
        log.info("获取作者详情请求：ID={}", id);
        return authorManageService.getAuthorInfo(id);
    }
}