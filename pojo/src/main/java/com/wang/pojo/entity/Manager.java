package com.wang.pojo.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 管理员表
 * @TableName manager
 */
@Data
@TableName("manager")
public class Manager implements Serializable {

    /**
     * 主键ID
     */
    @NotNull(message = "[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 昵称
     */
    @Size(max = 64, message = "昵称长度不能超过64")
    @ApiModelProperty("昵称")
    @Length(max = 64, message = "昵称长度不能超过64")
    private String name;

    /**
     * 账号
     */
    @NotBlank(message = "[账号]不能为空")
    @Size(max = 64, message = "账号长度不能超过64")
    @ApiModelProperty("账号")
    @Length(max = 64, message = "账号长度不能超过64")
    private String account;

    /**
     * 密码
     */
    @NotBlank(message = "[密码]不能为空")
    @Size(max = 128, message = "密码长度不能超过128")
    @ApiModelProperty("密码")
    @Length(max = 128, message = "密码长度不能超过128")
    private String password;

    /**
     * 创建者ID
     */
    @NotNull(message = "[创建者ID]不能为空")
    @ApiModelProperty("创建者ID")
    private Long createId;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

}