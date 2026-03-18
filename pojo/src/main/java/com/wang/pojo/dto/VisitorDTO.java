package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * 访客DTO类
 * 用于前端传递访客数据
 */
@Data
@ApiModel("访客DTO")
public class VisitorDTO {

    @ApiModelProperty("访客ID")
    private Integer id;


    @ApiModelProperty("访客名称")
    private String name;

    @ApiModelProperty("头像地址")
    private String avatar;

    @ApiModelProperty("账号（手机号）")
    private String account;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("邮箱")
    private String email;
}