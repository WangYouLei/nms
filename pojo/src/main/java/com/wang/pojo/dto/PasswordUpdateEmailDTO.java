package com.wang.pojo.dto;

import lombok.Data;


@Data
public class PasswordUpdateEmailDTO {
    /**
     * 主键ID（已登录用户修改密码时使用）
     */
    private Integer id;
    /**
     * 账号（忘记密码场景使用，与id二选一）
     */
    private String account;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 验证码
     */
    private String code;
    /**
     * 新密码
     */
    private String newPassword;
}
