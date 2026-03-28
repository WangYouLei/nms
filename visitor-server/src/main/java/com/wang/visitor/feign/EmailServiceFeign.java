package com.wang.visitor.feign;

import com.wang.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "common-server")
public interface EmailServiceFeign {

    @PostMapping("/email/sendCode")
    Result sendCode(@RequestParam("email") String email);

    @PostMapping("/email/verifyCode")
    Result verifyCode(@RequestParam("email") String email, @RequestParam("code") String code);
}
