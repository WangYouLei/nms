package com.wang.common.model;


import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class LoginUser {
    /**
     * 用户ID
     */
    private Integer id;
    /**
     * 用户名称
     */
    private String name;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 邮箱
     */
    private String account;
}
