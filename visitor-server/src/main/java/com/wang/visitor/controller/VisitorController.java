package com.wang.visitor.controller;

import com.wang.common.result.Result;
import com.wang.pojo.dto.VisitorDTO;
import com.wang.visitor.service.VisitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 访客控制器
 */
@Slf4j
@RestController
@RequestMapping("/visitor")
@Api(tags = "访客管理")
@Validated
public class VisitorController {

    private final VisitorService visitorService;


    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    /**
     * 访客注册
     */
    @PostMapping("/register")
    @ApiOperation("访客注册")
    public Result register(@RequestBody VisitorDTO visitorDTO) {
        log.info("访客注册请求：账号={}", visitorDTO.getAccount());
        return visitorService.register(visitorDTO);
    }

    /**
     * 访客登录
     */
    @PostMapping("/login")
    @ApiOperation("访客登录")
    public Result login(@RequestParam String account, @RequestParam String password) {
        log.info("访客登录请求：账号={}", account);
        return visitorService.login(account, password);
    }

    /**
     * 获取访客信息
     */
    @GetMapping("/info/{visitorId}")
    @ApiOperation("获取访客信息")
    public Result getVisitorInfo(@PathVariable Integer visitorId) {
        log.info("获取访客信息请求：ID={}", visitorId);
        return visitorService.getVisitorInfo(visitorId);
    }

    /**
     * 修改访客信息
     */
    @PutMapping("/update")
    @ApiOperation("修改访客信息")
    public Result updateVisitor(@RequestBody VisitorDTO visitorDTO) {
        log.info("修改访客信息请求：ID={}", visitorDTO.getId());
        return visitorService.updateVisitor(visitorDTO);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    @ApiOperation("修改密码")
    public Result updatePassword(
            @RequestParam Integer visitorId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        log.info("修改密码请求：访客ID={}", visitorId);
        return visitorService.updatePassword(visitorId, oldPassword, newPassword);
    }
}