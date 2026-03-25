package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 评论查询DTO类
 * 用于查询评论列表
 */
@Data
@ApiModel("评论查询DTO")
public class CommentQueryDTO {

    @ApiModelProperty("小说ID")
    private Long novelId;

    @ApiModelProperty("评论对象类型：1-小说，2-章节")
    private Integer targetType;

    @ApiModelProperty("评论对象ID（小说ID或章节ID）")
    private Long targetId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户类型：1-访客，2-作者，3-管理员")
    private Integer userType;

    @ApiModelProperty("父评论ID（查询回复时使用）")
    private Long parentId;

    @ApiModelProperty("根评论ID（查询某条评论的所有回复）")
    private Long rootId;

    @ApiModelProperty("审核层级：0-未审核，1-本地过滤通过，2-人工审核通过")
    private Integer auditLevel;

    @ApiModelProperty("页码（默认1）")
    private Integer pageNum = 1;

    @ApiModelProperty("每页数量（默认10）")
    private Integer pageSize = 10;
}