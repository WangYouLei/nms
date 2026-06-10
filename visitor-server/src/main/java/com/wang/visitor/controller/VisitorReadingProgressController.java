package com.wang.visitor.controller;

import com.wang.common.model.LoginUser;
import com.wang.common.result.Result;
import com.wang.common.utils.RoleContextUtil;
import com.wang.visitor.service.VisitorReadingProgressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Slf4j
@RestController
@Validated
@Api(tags = "阅读进度管理")
@RequestMapping("/visitor/reading-progress")
public class VisitorReadingProgressController {

    private final VisitorReadingProgressService visitorReadingProgressService;

    public VisitorReadingProgressController(VisitorReadingProgressService visitorReadingProgressService) {
        this.visitorReadingProgressService = visitorReadingProgressService;
    }

    @PostMapping("/update")
    @ApiOperation("更新阅读进度")
    public Result updateProgress(@RequestParam @NotNull Long novelId,
                                  @RequestParam @NotNull Long chapterId,
                                  @RequestParam @NotNull @Min(1) Integer chapterOrder) {
        LoginUser loginUser = RoleContextUtil.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        log.info("更新阅读进度：visitorId={}, novelId={}, chapterId={}", loginUser.getId(), novelId, chapterId);
        return visitorReadingProgressService.updateProgress(loginUser.getId(), novelId, chapterId, chapterOrder);
    }

    @GetMapping("/{novelId}")
    @ApiOperation("获取阅读进度")
    public Result getProgress(@PathVariable Long novelId) {
        LoginUser loginUser = RoleContextUtil.getCurrentUser();
        if (loginUser == null) {
            return Result.success(null);
        }
        return visitorReadingProgressService.getProgress(loginUser.getId(), novelId);
    }

    @GetMapping("/recent")
    @ApiOperation("获取最近阅读列表")
    public Result getRecentList() {
        LoginUser loginUser = RoleContextUtil.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        log.info("获取最近阅读列表：visitorId={}", loginUser.getId());
        return visitorReadingProgressService.getRecentList(loginUser.getId());
    }

    @DeleteMapping("/{novelId}")
    @ApiOperation("删除阅读进度")
    public Result deleteProgress(@PathVariable Long novelId) {
        LoginUser loginUser = RoleContextUtil.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        log.info("删除阅读进度：visitorId={}, novelId={}", loginUser.getId(), novelId);
        return visitorReadingProgressService.deleteProgress(loginUser.getId(), novelId);
    }

    @GetMapping("/count/{novelId}")
    @ApiOperation("获取小说在读人数")
    public Result getReadingCount(@PathVariable Long novelId) {
        return visitorReadingProgressService.getReadingCount(novelId);
    }
}
