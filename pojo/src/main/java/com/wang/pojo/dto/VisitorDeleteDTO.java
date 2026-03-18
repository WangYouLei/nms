package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 访客删除DTO类
 * 用于通过邮箱验证码删除访客账号
 */
@Data
@ApiModel("访客删除DTO")
public class VisitorDeleteDTO {

    @ApiModelProperty("访客ID")
    private Integer id;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("验证码")
    private String code;
}