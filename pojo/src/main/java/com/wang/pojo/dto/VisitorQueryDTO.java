package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 访客查询DTO
 * 用于多条件分页查询访客信息
 */
@Data
@ApiModel("访客查询条件")
public class VisitorQueryDTO implements Serializable {

    /**
     * 访客ID
     */
    @ApiModelProperty("访客ID")
    private Integer id;

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
     * VIP等级
     */
    @ApiModelProperty("VIP等级")
    private Integer vipLevel;

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