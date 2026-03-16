package com.wang.commonserver.controller;

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
    public Map<String, Object> generateCaptcha() {
        return captchaService.generateCaptcha();
    }

    /**
     * 校验验证码
     * @param token 验证码 token
     * @param code 用户输入的验证码
     * @return 是否验证成功
     */
    @RequestMapping("/verify")
    public boolean verify(String token, String code) {
        return captchaService.verify(token, code);
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
