package com.wang.pojo.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
* 访问者表
* @TableName visitor
*/
@Data
@TableName("visitor")
public class Visitor implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
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
    * 邮箱
    */
    @Size(max= 50,message="编码长度不能超过50")
    @ApiModelProperty("邮箱")
    @Length(max= 50,message="编码长度不能超过50")
    private String email;

/**
     * VIP级别：0-普通,1-VIP1,2-VIP2,3-VIP3,4-金主
     */
    @NotNull(message="[VIP级别]不能为空")
    @ApiModelProperty("VIP级别：0-普通,1-VIP1,2-VIP2,3-VIP3,4-金主")
    private Integer vipLevel;

    /**
     * 是否删除：false-否，true-是
     */
    @ApiModelProperty("是否删除：false-否，true-是")
    @TableLogic
    private Boolean isDel;

    /**
     * 创建时间
     */
    @NotNull(message="[创建时间]不能为空")
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
    * 修改时间
    */
    @NotNull(message="[修改时间]不能为空")
    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

}