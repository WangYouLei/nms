package com.wang.manager.controller;

import com.wang.common.result.Result;
import com.wang.manager.service.VisitorManageService;
import com.wang.pojo.dto.VisitorQueryDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Api(tags = "访客管理")
@RequestMapping("/manager/visitor")
public class VisitorManageController {

    private final VisitorManageService visitorManageService;

    @Autowired
    public VisitorManageController(VisitorManageService visitorManageService) {
        this.visitorManageService = visitorManageService;
    }

    @GetMapping("/page")
    @ApiOperation("分页查询访客信息")
    public Result getVisitorPage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("分页查询访客请求：页码={}, 每页数量={}", pageNum, pageSize);
        return visitorManageService.getVisitorPage(pageNum, pageSize);
    }

    @GetMapping("/list")
    @ApiOperation("多条件查询访客（支持id、姓名、账号、VIP等级，条件可为空）")
    public Result getVisitorList(VisitorQueryDTO queryDTO) {
        log.info("多条件查询访客请求：queryDTO={}", queryDTO);
        return visitorManageService.getVisitorList(queryDTO);
    }

    @GetMapping("/info/{id}")
    @ApiOperation("获取访客详情")
    public Result getVisitorInfo(@PathVariable Long id) {
        log.info("获取访客详情请求：ID={}", id);
        return visitorManageService.getVisitorInfo(id);
    }
}