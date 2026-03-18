package com.wang.visitor.controller;

import com.wang.common.result.Result;
import com.wang.pojo.dto.VisitorDTO;
import com.wang.pojo.dto.VisitorRegisterDTO;
import com.wang.visitor.service.VisitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
     * 访客注册（带验证码）
     */
    @PostMapping("/register")
    @ApiOperation("访客注册（带验证码）")
    public Result register(@RequestBody VisitorRegisterDTO registerDTO) {
        log.info("访客注册请求：账号={}", registerDTO.getAccount());
        return visitorService.register(registerDTO);
    }

    /**
     * 访客注册（无验证码，保留兼容）
     */
    @PostMapping("/register/simple")
    @ApiOperation("访客注册（无验证码，保留兼容）")
    public Result registerSimple(@RequestBody VisitorDTO visitorDTO) {
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
    public Result updateVisitor(
            @ApiParam(value = "访客id")
            @RequestParam
            Integer visitorId,
            @ApiParam(value = "访客姓名")
            @RequestParam
            String name) {
        log.info("修改访客信息请求：ID={},name={}", visitorId, name);
        return visitorService.updateVisitor(visitorId, name);
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