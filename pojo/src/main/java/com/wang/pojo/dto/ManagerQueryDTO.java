package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 管理员查询DTO
 * 用于多条件分页查询管理员信息
 */
@Data
@ApiModel("管理员查询条件")
public class ManagerQueryDTO implements Serializable {

    /**
     * 管理员ID
     */
    @ApiModelProperty("管理员ID")
    private Long id;

    /**
     * 昵称（模糊查询）
     */
    @ApiModelProperty("昵称")
    private String name;

    /**
     * 账号（模糊查询）
     */
    @ApiModelProperty("账号")
    private String account;

    /**
     * 页码
     */
    @ApiModelProperty("页码")
    private Integer pageNum;

    /**
     * 每页数量
     */
    @ApiModelProperty("每页数量")
    private Integer pageSize;
}