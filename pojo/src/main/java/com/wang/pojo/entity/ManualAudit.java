package com.wang.pojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 人工审核表
 * @TableName manual_audit
 */
@Data
@TableName("manual_audit")
public class ManualAudit implements Serializable {

    /**
     * 审核记录ID
     */
    @ApiModelProperty("审核记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 审核目标对象ID
     */
    @ApiModelProperty("审核目标对象ID")
    private Long aimId;

    /**
     * 审核目标对象类型
     */
    @ApiModelProperty("审核目标对象类型")
    private Integer aimType;

    /**
     * 人工审核结果：0-待审核，1-通过，2-拒绝
     */
    @ApiModelProperty("人工审核结果：0-待审核，1-通过，2-拒绝")
    private Integer result;

    /**
     * 拒绝理由
     */
    @ApiModelProperty("拒绝理由")
    private String refusalReason;

    /**
     * AI审核结果意见
     */
    @ApiModelProperty("AI审核结果意见")
    private String aiResult;

    /**
     * 初审核的管理员ID
     */
    @ApiModelProperty("初审核的管理员ID")
    private Long managerId;

    /**
     * 初审核的管理员昵称
     */
    @ApiModelProperty("初审核的管理员昵称")
    private String managerName;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 初审核时间
     */
    @ApiModelProperty("初审核时间")
    private LocalDateTime firstAuditTime;

    /**
     * 修改时间
     */
    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

}
