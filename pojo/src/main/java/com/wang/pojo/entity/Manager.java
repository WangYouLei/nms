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
 * 管理员表
 * @TableName manager
 */
@Data
@TableName("manager")
public class Manager implements Serializable {

    /**
     * 主键ID
     */
    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 昵称
     */
    @ApiModelProperty("昵称")
    private String name;

    /**
     * 账号
     */
    @ApiModelProperty("账号")
    private String account;

    /**
     * 密码
     */
    @ApiModelProperty("密码")
    private String password;

    /**
     * 头像
     */
    @ApiModelProperty("头像")
    private String avatar;

    /**
     * 创建者ID
     */
    @ApiModelProperty("创建者ID")
    private Long createId;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    /**
     * 是否删除（false未删除，true已删除）
     */
    @ApiModelProperty("是否删除（false未删除，true已删除）")
    @TableLogic
    private Boolean isDel;

}
