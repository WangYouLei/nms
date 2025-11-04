package com.wang.pojo.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import java.time.LocalDateTime;
import java.util.Date;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
* 管理人信息表
* @TableName manager
*/
@Data
public class Manager implements Serializable {

    /**
    * 管理人ID
    */
    @NotNull(message="[管理人ID]不能为空")
    @ApiModelProperty("管理人ID")
    private Integer id;
    /**
    * 管理人名称
    */
    @NotBlank(message="[管理人名称]不能为空")
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("管理人名称")
    @Length(max= 20,message="编码长度不能超过20")
    private String name;
    /**
    * 账号(手机号)
    */
    @NotBlank(message="[账号(手机号)]不能为空")
    @Size(max= 11,message="编码长度不能超过11")
    @ApiModelProperty("账号(手机号)")
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
    * 头像URL
    */
    @NotBlank(message="[头像URL]不能为空")
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("头像URL")
    @Length(max= 255,message="编码长度不能超过255")
    private String avatar;
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
    /**
    * 盐
    */
    @Size(max= 12,message="编码长度不能超过12")
    @ApiModelProperty("盐")
    @Length(max= 12,message="编码长度不能超过12")
    private String secret;
    /**
    * 创建本账号的管理者
    */
    @ApiModelProperty("创建本账号的管理者")
    private Integer createManager;
    /**
    * 修改本账号的管理者
    */
    @ApiModelProperty("修改本账号的管理者")
    private Integer updateManager;


}
