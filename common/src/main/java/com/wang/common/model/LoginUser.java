package com.wang.common.model;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

