package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull(message = "[审核目标对象ID]不能为空")
    @ApiModelProperty(value = "审核目标对象ID", required = true)
    private Long aimId;

    @NotNull(message = "[审核目标对象类型]不能为空")
    @ApiModelProperty(value = "审核目标对象类型：1-评论，2-小说，3-章节", required = true)
    private Integer aimType;

    @ApiModelProperty("人工审核结果：0-待审核，1-通过，2-拒绝")
    private Integer result;

    @Size(max = 255, message = "拒绝理由长度不能超过255")
    @ApiModelProperty("拒绝理由")
    private String refusalReason;

    @ApiModelProperty("审核管理员ID")
    private Integer managerId;

    @Size(max = 50, message = "管理员昵称长度不能超过50")
    @ApiModelProperty("审核管理员昵称")
    private String managerName;
}