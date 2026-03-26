package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 人工审核查询DTO类
 * 用于查询人工审核列表
 */
@Data
@ApiModel("人工审核查询DTO")
public class ManualAuditQueryDTO {

    @ApiModelProperty("审核目标对象ID")
    private Long aimId;

    @ApiModelProperty("审核目标对象类型：1-评论，2-小说，3-章节")
    private Integer aimType;

    @ApiModelProperty("人工审核结果：0-待审核，1-通过，2-拒绝")
    private Integer result;

    @ApiModelProperty("审核管理员ID")
    private Integer managerId;

    @ApiModelProperty("页码（默认1）")
    private Integer pageNum = 1;

    @ApiModelProperty("每页数量（默认10）")
    private Integer pageSize = 10;
}