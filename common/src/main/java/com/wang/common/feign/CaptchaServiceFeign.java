package com.wang.common.feign;

import com.wang.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "common-server")
public interface CaptchaServiceFeign {

    @PostMapping("/captcha/generate")
    Result generateCaptcha();

    @PostMapping("/captcha/verify")
    Result verify(@RequestParam("token") String token, @RequestParam("code") String code);
}
