package com.wang.manage.feign;

import com.wang.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "common-server")
public interface CommonServiceFeignService {

    /**
     * 邮箱验证码验证
     * @param email 邮箱
     * @param code 验证码
     * @return
     */
    @PostMapping("/email/verifyCode")
    public Result verifyCode(@RequestParam String email, @RequestParam String code);

    /**
     * 图形验证码校验
     * @param token token
     * @param code 用户输入的验证码
     * @return
     */
    @RequestMapping("/verify")
    Result verify(@RequestParam("token") String token, @RequestParam("code") String code);
}
