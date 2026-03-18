package com.wang.pojo.dto;

import lombok.Data;


@Data
public class PasswordUpdateEmailDTO {
    /**
     * 主键ID
     */
    private Integer id;
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
