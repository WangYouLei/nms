package com.wang.pojo.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

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
    @NotNull(message = "[审核记录ID]不能为空")
    @ApiModelProperty("审核记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 审核目标对象ID
     */
    @NotNull(message = "[审核目标对象ID]不能为空")
    @ApiModelProperty("审核目标对象ID")
    private Long aimId;

    /**
     * 审核目标对象类型
     */
    @NotNull(message = "[审核目标对象类型]不能为空")
    @ApiModelProperty("审核目标对象类型")
    private Integer aimType;

    /**
     * 人工审核结果：0-待审核，1-通过，2-拒绝
     */
    @NotNull(message = "[人工审核结果]不能为空")
    @ApiModelProperty("人工审核结果：0-待审核，1-通过，2-拒绝")
    private Integer result;

    /**
     * 拒绝理由
     */
    @Size(max = 255, message = "拒绝理由长度不能超过255")
    @ApiModelProperty("拒绝理由")
    @Length(max = 255, message = "拒绝理由长度不能超过255")
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
    private Integer managerId;

    /**
     * 初审核的管理员昵称
     */
    @Size(max = 50, message = "管理员昵称长度不能超过50")
    @ApiModelProperty("初审核的管理员昵称")
    @Length(max = 50, message = "管理员昵称长度不能超过50")
    private String managerName;

    /**
     * 创建时间
     */
    @NotNull(message = "[创建时间]不能为空")
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
    @NotNull(message = "[修改时间]不能为空")
    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

}