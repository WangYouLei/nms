package com.wang.commonserver.controller;

import com.wang.common.result.Result;
import com.wang.commonserver.service.EmailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Api(tags = "邮箱服务")
@RequestMapping("/email")
public class EmailController {
    
    private final EmailService emailService;
    
    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }
    
    @PostMapping("/sendCode")
    @ApiOperation("发送邮箱验证码")
    public Result sendCode(@RequestParam String email) {
        return emailService.sendCode(email);
    }

    @PostMapping("/verifyCode")
    @ApiOperation("验证邮箱验证码")
    public Result verifyCode(@RequestParam String email, @RequestParam String code) {
        return emailService.verifyCode(email, code);
    }

}