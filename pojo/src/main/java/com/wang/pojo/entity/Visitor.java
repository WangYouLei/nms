package com.wang.pojo.entity;

import java.io.Serializable;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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
    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
    * 访问者名称
    */
    @ApiModelProperty("访问者名称")
    private String name;

    /**
    * 头像地址（URL）
    */
    @ApiModelProperty("头像地址（URL）")
    private String avatar;

    /**
    * 账号（手机号）
    */
    @ApiModelProperty("账号（手机号）")
    private String account;

    /**
    * 密码
    */
    @ApiModelProperty("密码")
    private String password;

    /**
    * 邮箱
    */
    @ApiModelProperty("邮箱")
    private String email;

/**
     * VIP级别：0-普通,1-VIP1,2-VIP2,3-VIP3,4-金主
     */
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
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
    * 修改时间
    */
    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

}
