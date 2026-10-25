package com.wang.visitor.controller;

import com.wang.common.enums.BizCodeEnum;
import com.wang.common.result.Result;
import com.wang.common.utils.RoleContextUtil;
import com.wang.pojo.dto.VisitorFollowDTO;
import com.wang.visitor.service.VisitorFollowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 访客关注作者控制器
 * 访客身份一律取当前登录用户，不信任前端传入的visitorId，防止越权操作他人关注数据
 */
@Slf4j
@RestController
@Api(tags = "访客关注管理")
@RequestMapping("/visitor/follow")
public class VisitorFollowController {

    private final VisitorFollowService visitorFollowService;

    @Autowired
    public VisitorFollowController(VisitorFollowService visitorFollowService) {
        this.visitorFollowService = visitorFollowService;
    }

    /**
     * 获取当前登录访客ID
     * @return 访客ID，未登录返回null
     */
    private Long getCurrentVisitorId() {
        return RoleContextUtil.getCurrentUserId();
    }

    @PostMapping("/add")
    @ApiOperation("关注作者")
    public Result follow(@RequestBody @ApiParam("关注信息") VisitorFollowDTO dto) {
        Long visitorId = getCurrentVisitorId();
        if (visitorId == null) {
            return Result.buildResult(BizCodeEnum.USER_NOT_LOGIN);
        }
        // 强制使用当前登录访客身份，忽略前端传入的visitorId
        dto.setVisitorId(visitorId);
        log.info("关注作者请求：visitorId={}, authorId={}", visitorId, dto.getAuthorId());
        return visitorFollowService.follow(dto);
    }

    @DeleteMapping("/remove/{authorId}")
    @ApiOperation("取消关注")
    public Result unfollow(
            @PathVariable @ApiParam("作者ID") Long authorId,
            @RequestParam @ApiParam("访客ID") Long visitorId) {
        Long currentVisitorId = getCurrentVisitorId();
        if (currentVisitorId == null) {
            return Result.buildResult(BizCodeEnum.USER_NOT_LOGIN);
        }
        log.info("取消关注请求：visitorId={}, authorId={}", currentVisitorId, authorId);
        return visitorFollowService.unfollow(currentVisitorId, authorId);
    }

    @GetMapping("/check/{authorId}")
    @ApiOperation("检查是否已关注")
    public Result checkFollow(
            @PathVariable @ApiParam("作者ID") Long authorId,
            @RequestParam @ApiParam("访客ID") Long visitorId) {
        Long currentVisitorId = getCurrentVisitorId();
        if (currentVisitorId == null) {
            return Result.buildResult(BizCodeEnum.USER_NOT_LOGIN);
        }
        log.info("检查是否关注请求：visitorId={}, authorId={}", currentVisitorId, authorId);
        return visitorFollowService.checkFollow(currentVisitorId, authorId);
    }

    @GetMapping("/list")
    @ApiOperation("获取我的关注列表")
    public Result getMyFollows(
            @RequestParam @ApiParam("访客ID") Long visitorId,
            @RequestParam(defaultValue = "1") @ApiParam("页码") Integer pageNum,
            @RequestParam(defaultValue = "10") @ApiParam("每页数量") Integer pageSize) {
        Long currentVisitorId = getCurrentVisitorId();
        if (currentVisitorId == null) {
            return Result.buildResult(BizCodeEnum.USER_NOT_LOGIN);
        }
        log.info("获取我的关注列表请求：visitorId={}", currentVisitorId);
        return visitorFollowService.getMyFollows(currentVisitorId, pageNum, pageSize);
    }

    @GetMapping("/followers/{authorId}")
    @ApiOperation("获取作者的粉丝列表")
    public Result getFollowers(
            @PathVariable @ApiParam("作者ID") Long authorId,
            @RequestParam(defaultValue = "1") @ApiParam("页码") Integer pageNum,
            @RequestParam(defaultValue = "10") @ApiParam("每页数量") Integer pageSize) {
        log.info("获取作者粉丝列表请求：authorId={}", authorId);
        return visitorFollowService.getFollowers(authorId, pageNum, pageSize);
    }

    @GetMapping("/count")
    @ApiOperation("获取我的关注数量")
    public Result getMyFollowCount(@RequestParam @ApiParam("访客ID") Long visitorId) {
        Long currentVisitorId = getCurrentVisitorId();
        if (currentVisitorId == null) {
            return Result.buildResult(BizCodeEnum.USER_NOT_LOGIN);
        }
        log.info("获取我的关注数量请求：visitorId={}", currentVisitorId);
        return visitorFollowService.getMyFollowCount(currentVisitorId);
    }

    @GetMapping("/followerCount/{authorId}")
    @ApiOperation("获取作者的粉丝数量")
    public Result getFollowerCount(@PathVariable @ApiParam("作者ID") Long authorId) {
        log.info("获取作者粉丝数量请求：authorId={}", authorId);
        return visitorFollowService.getFollowerCount(authorId);
    }
}
