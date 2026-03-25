package com.wang.pojo.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 敏感词表
 * @TableName sensitive_word
 */
@Data
public class SensitiveWord implements Serializable {

    /**
     * 敏感词ID
     */
    @NotNull(message = "[敏感词ID]不能为空")
    @ApiModelProperty("敏感词ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 敏感词
     */
    @NotBlank(message = "[敏感词]不能为空")
    @Size(max = 100, message = "编码长度不能超过100")
    @ApiModelProperty("敏感词")
    @Length(max = 100, message = "编码长度不能超过100")
    private String word;

    /**
     * 敏感词类别：1-涉政，2-涉黄，3-涉暴，4-广告，5-其他
     */
    @NotNull(message = "[敏感词类别]不能为空")
    @ApiModelProperty("敏感词类别：1-涉政，2-涉黄，3-涉暴，4-广告，5-其他")
    private Integer category;

    /**
     * 敏感等级：1-低（需人工审核），2-高（直接拒绝）
     */
    @NotNull(message = "[敏感等级]不能为空")
    @ApiModelProperty("敏感等级：1-低（需人工审核），2-高（直接拒绝）")
    private Integer level;

    /**
     * 状态：0-禁用，1-启用
     */
    @NotNull(message = "[状态]不能为空")
    @ApiModelProperty("状态：0-禁用，1-启用")
    private Integer status;

    /**
     * 来源：1-系统内置，2-管理员添加
     */
    @NotNull(message = "[来源]不能为空")
    @ApiModelProperty("来源：1-系统内置，2-管理员添加")
    private Integer source;

    /**
     * 创建人ID（管理员添加时）
     */
    @ApiModelProperty("创建人ID（管理员添加时）")
    private Long creatorId;

    /**
     * 创建时间
     */
    @NotNull(message = "[创建时间]不能为空")
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @NotNull(message = "[更新时间]不能为空")
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

}