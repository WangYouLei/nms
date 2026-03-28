package com.wang.commonserver.controller;

import com.wang.common.result.Result;
import com.wang.commonserver.service.CaptchaService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;


@RestController
@Slf4j
@Api(tags = "验证码服务")
@RequestMapping("/captcha")
public class CaptchaController {
    private final CaptchaService captchaService;
    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }
    /**
     * 生成验证码
     * @return Map 验证码信息
     */
    @RequestMapping("/generate")
    public Result generateCaptcha() {
        Map<String, Object> map = captchaService.generateCaptcha();
        if (map != null) {
            return Result.success(map);
        }else{
            return Result.error("验证码生成失败");
        }
    }

    /**
     * 校验验证码
     * @param token 验证码 token
     * @param code 用户输入的验证码
     * @return 是否验证成功
     */
    @RequestMapping("/verify")
    public Result verify(String token, String code) {
        boolean valid = captchaService.verify(token, code);
        if (valid) {
            return Result.success();
        }
        return Result.error("验证码错误或已过期");
    }

    /**
     * 删除验证码（用于手动失效）
     * @param token 验证码 token
     */
    @RequestMapping("/remove")
    public void removeCaptcha(String token) {
        captchaService.removeCaptcha(token);
    }
}