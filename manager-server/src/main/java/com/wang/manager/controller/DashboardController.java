package com.wang.manager.controller;

import com.wang.common.result.Result;
import com.wang.manager.service.DashboardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据概览Controller
 */
@Slf4j
@RestController
@Api(tags = "数据概览")
@RequestMapping("/manager/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    @ApiOperation("获取概览数据")
    public Result getOverview() {
        log.info("获取数据概览请求");
        return dashboardService.getOverview();
    }
}