package com.wang.commonserver.service;


import java.util.Map;

public interface CaptchaService {
    /**
     * 生成图形验证码
     * @return Map 验证码信息
     */
    Map<String, Object> generateCaptcha();

    /**
     * 校验验证码
     * @param token 验证码 token
     * @param code 用户输入的验证码
     * @return 是否验证成功
     */
    boolean verify(String token, String code);

    /**
     * 删除验证码（用于手动失效）
     * @param token 验证码 token
     */
    void removeCaptcha(String token);
}
