package com.wang.pojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 敏感词表
 * @TableName sensitive_word
 */
@Data
public class SensitiveWord implements Serializable {

    /**
     * 敏感词ID
     */
    @ApiModelProperty("敏感词ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 敏感词
     */
    @ApiModelProperty("敏感词")
    private String word;

    /**
     * 敏感词类别：1-涉政，2-涉黄，3-涉暴，4-广告，5-其他
     */
    @ApiModelProperty("敏感词类别：1-涉政，2-涉黄，3-涉暴，4-广告，5-其他")
    private Integer category;

    /**
     * 敏感等级：1-低（需人工审核），2-高（直接拒绝）
     */
    @ApiModelProperty("敏感等级：1-低（需人工审核），2-高（直接拒绝）")
    private Integer level;

    /**
     * 状态：0-禁用，1-启用
     */
    @ApiModelProperty("状态：0-禁用，1-启用")
    private Integer status;

    /**
     * 来源：1-系统内置，2-管理员添加
     */
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
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

}
