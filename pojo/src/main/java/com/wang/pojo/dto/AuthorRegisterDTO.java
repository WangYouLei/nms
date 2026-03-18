package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 作者注册DTO
 * 包含验证码字段
 */
@Data
@ApiModel("作者注册DTO")
public class AuthorRegisterDTO implements Serializable {

    @ApiModelProperty(value = "作者昵称", required = true)
    private String name;

    @ApiModelProperty(value = "账号(手机号)", required = true)
    private String account;

    @ApiModelProperty(value = "密码", required = true)
    private String password;

    @ApiModelProperty(value = "邮箱", required = true)
    private String email;

    @ApiModelProperty(value = "图形验证码Token", required = true)
    private String captchaToken;

    @ApiModelProperty(value = "图形验证码", required = true)
    private String captchaCode;

    @ApiModelProperty(value = "邮箱验证码", required = true)
    private String emailCode;
}