package com.wang.commonserver.service;

import com.wang.common.result.Result;

public interface EmailService {
    
    /**
     * 发送验证码到邮箱
     * @param email 目标邮箱地址
     * @return 发送结果
     */
    Result sendCode(String email);
    
    /**
     * 验证邮箱验证码
     * @param email 邮箱地址
     * @param code 用户输入的验证码
     * @return 验证结果
     */
    Result verifyCode(String email, String code);
}