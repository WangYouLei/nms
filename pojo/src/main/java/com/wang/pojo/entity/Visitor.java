package com.wang.pojo.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
* 访问者表
* @TableName visitor
*/
@Data
public class Visitor implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    private Integer id;
    /**
    * 访问者名称
    */
    @NotBlank(message="[访问者名称]不能为空")
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("访问者名称")
    @Length(max= 20,message="编码长度不能超过20")
    private String name;
    /**
    * 头像地址（URL）
    */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("头像地址（URL）")
    @Length(max= 255,message="编码长度不能超过255")
    private String avatar;
    /**
    * 账号（手机号）
    */
    @NotBlank(message="[账号（手机号）]不能为空")
    @Size(max= 11,message="编码长度不能超过11")
    @ApiModelProperty("账号（手机号）")
    @Length(max= 11,message="编码长度不能超过11")
    private String account;
    /**
    * 密码
    */
    @NotBlank(message="[密码]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @ApiModelProperty("密码")
    @Length(max= 100,message="编码长度不能超过100")
    private String password;
    /**
    * VIP级别：0-普通,1-VIP1,2-VIP2,3-VIP3,4-金主
    */
    @NotNull(message="[VIP级别：0-普通,1-VIP1,2-VIP2,3-VIP3,4-金主]不能为空")
    @ApiModelProperty("VIP级别：0-普通,1-VIP1,2-VIP2,3-VIP3,4-金主")
    private Integer vipLevel;

}
