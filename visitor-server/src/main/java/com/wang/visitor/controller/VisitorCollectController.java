package com.wang.visitor.controller;

import com.wang.common.result.Result;
import com.wang.common.utils.RoleContextUtil;
import com.wang.common.model.LoginUser;
import com.wang.visitor.service.VisitorCollectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 小说收藏控制器
 */
@Slf4j
@RestController
@Api(tags = "小说收藏管理")
@RequestMapping("/visitor/collect")
public class VisitorCollectController {

    private final VisitorCollectService visitorCollectService;

    public VisitorCollectController(VisitorCollectService visitorCollectService) {
        this.visitorCollectService = visitorCollectService;
    }

    @PostMapping("/add/{novelId}")
    @ApiOperation("添加收藏")
    public Result addCollect(@PathVariable Long novelId) {
        LoginUser loginUser = RoleContextUtil.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        log.info("添加收藏：用户ID={}, 小说ID={}", loginUser.getId(), novelId);
        return visitorCollectService.addCollect(loginUser.getId(), novelId);
    }

    @DeleteMapping("/remove/{novelId}")
    @ApiOperation("取消收藏")
    public Result removeCollect(@PathVariable Long novelId) {
        LoginUser loginUser = RoleContextUtil.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        log.info("取消收藏：用户ID={}, 小说ID={}", loginUser.getId(), novelId);
        return visitorCollectService.removeCollect(loginUser.getId(), novelId);
    }

    @GetMapping("/list")
    @ApiOperation("获取收藏列表")
    public Result getCollectList() {
        LoginUser loginUser = RoleContextUtil.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        log.info("获取收藏列表：用户ID={}", loginUser.getId());
        return visitorCollectService.getCollectList(loginUser.getId());
    }

    @GetMapping("/check/{novelId}")
    @ApiOperation("检查是否已收藏")
    public Result checkCollect(@PathVariable Long novelId) {
        LoginUser loginUser = RoleContextUtil.getCurrentUser();
        if (loginUser == null) {
            return Result.success(false);
        }
        return visitorCollectService.checkCollect(loginUser.getId(), novelId);
    }

    @GetMapping("/count")
    @ApiOperation("获取收藏数量")
    public Result getCollectCount() {
        LoginUser loginUser = RoleContextUtil.getCurrentUser();
        if (loginUser == null) {
            return Result.success(0);
        }
        return visitorCollectService.getCollectCount(loginUser.getId());
    }
}