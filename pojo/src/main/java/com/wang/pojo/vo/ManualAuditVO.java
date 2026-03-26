package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 人工审核VO类
 * 用于后端返回人工审核数据
 */
@Data
@ApiModel("人工审核VO")
public class ManualAuditVO {

    @ApiModelProperty("审核记录ID")
    private Long id;

    @ApiModelProperty("审核目标对象ID")
    private Long aimId;

    @ApiModelProperty("审核目标对象类型：1-评论，2-小说，3-章节")
    private Integer aimType;

    @ApiModelProperty("审核目标对象类型名称")
    private String aimTypeName;

    @ApiModelProperty("人工审核结果：0-待审核，1-通过，2-拒绝")
    private Integer result;

    @ApiModelProperty("审核结果名称")
    private String resultName;

    @ApiModelProperty("拒绝理由")
    private String refusalReason;

    @ApiModelProperty("AI审核结果意见")
    private String aiResult;

    @ApiModelProperty("审核管理员ID")
    private Integer managerId;

    @ApiModelProperty("审核管理员昵称")
    private String managerName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("初审核时间")
    private LocalDateTime firstAuditTime;

    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

    // ========== 扩展字段 ==========

    @ApiModelProperty("审核目标内容（评论内容/小说标题/章节标题）")
    private String aimContent;

    @ApiModelProperty("提交人ID")
    private Long submitterId;

    @ApiModelProperty("提交人昵称")
    private String submitterName;
}