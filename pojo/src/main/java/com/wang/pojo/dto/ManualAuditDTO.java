package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 人工审核DTO类
 * 用于前端传递人工审核数据
 */
@Data
@ApiModel("人工审核DTO")
public class ManualAuditDTO {

    @ApiModelProperty("审核记录ID（更新时需要）")
    private Long id;

    @ApiModelProperty(value = "审核目标对象ID", required = true)
    private Long aimId;

    @ApiModelProperty(value = "审核目标对象类型：1-评论，2-小说，3-章节", required = true)
    private Integer aimType;

    @ApiModelProperty("人工审核结果：0-待审核，1-通过，2-拒绝")
    private Integer result;

    @ApiModelProperty("拒绝理由")
    private String refusalReason;

    @ApiModelProperty("审核管理员ID")
    private Long managerId;

    @ApiModelProperty("审核管理员昵称")
    private String managerName;
}
