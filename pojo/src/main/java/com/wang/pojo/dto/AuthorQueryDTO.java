package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 作者查询DTO
 * 用于多条件分页查询作者信息
 */
@Data
@ApiModel("作者查询条件")
public class AuthorQueryDTO implements Serializable {

    /**
     * 作者ID
     */
    @ApiModelProperty("作者ID")
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
     * 等级
     */
    @ApiModelProperty("等级")
    private Integer rank;

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