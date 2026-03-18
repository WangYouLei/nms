package com.wang.visitor.controller;

import com.wang.common.result.Result;
import com.wang.pojo.dto.PasswordUpdateEmailDTO;
import com.wang.pojo.dto.VisitorDeleteDTO;
import com.wang.pojo.dto.VisitorDTO;
import com.wang.pojo.dto.VisitorRegisterDTO;
import com.wang.visitor.service.VisitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Api(tags = "访客管理")
@RequestMapping("/visitor")
public class VisitorController {

    private final VisitorService visitorService;

    @Autowired
    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }


    @PostMapping("/login")
    @ApiOperation("访客登录")
    public Result login(@RequestParam String account, @RequestParam String password) {
        log.info("访客登录请求：账号={}", account);
        return visitorService.login(account, password);
    }

    @PostMapping("logout")
    @ApiOperation("用户退出登入")
    public Result logout(Integer id){
        log.info("用户退出登入,id= {}", id);
        return Result.success();
    }

    @PostMapping("/register")
    @ApiOperation("访客注册（带验证码）")
    public Result register(@RequestBody VisitorRegisterDTO registerDTO) {
        log.info("访客注册请求：账号={}", registerDTO.getAccount());
        return visitorService.register(registerDTO);
    }


    @GetMapping("/info/{visitorId}")
    @ApiOperation("获取访客信息")
    public Result getVisitorInfo(@PathVariable Integer visitorId) {
        log.info("获取访客信息请求：ID={}", visitorId);
        return visitorService.getVisitorInfo(visitorId);
    }


    @PutMapping("/update")
    @ApiOperation("修改访客信息（不包括密码）")
    public Result updateVisitor(@RequestBody VisitorDTO visitor) {
        log.info("修改访客信息请求：ID={}", visitor.getId());
        return visitorService.updateVisitor(visitor);
    }


    @PostMapping("/password")
    @ApiOperation("修改密码")
    public Result updatePassword(
            @ApiParam("访客ID") @RequestParam Integer visitorId,
            @ApiParam("旧密码") @RequestParam String oldPassword,
            @ApiParam("新密码") @RequestParam String newPassword) {
        log.info("修改访客密码请求：ID={}", visitorId);
        return visitorService.updatePassword(visitorId, oldPassword, newPassword);
    }

    @PostMapping("/updatePasswordByEmail")
    @ApiOperation("通过邮箱短信验证码修改密码")
    public Result updatePasswordByEmail(@RequestBody @ApiParam("密码修改邮箱类") PasswordUpdateEmailDTO dto) {
        log.info("通过邮箱短信验证码修改密码请求：ID={}", dto.getId());
        return visitorService.updatePasswordByEmail(dto);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除访客账号（通过邮箱验证码验证）")
    public Result deleteVisitor(@RequestBody @ApiParam("访客删除DTO") VisitorDeleteDTO dto) {
        log.info("删除访客账号请求：ID={}", dto.getId());
        return visitorService.deleteVisitor(dto);
    }
}