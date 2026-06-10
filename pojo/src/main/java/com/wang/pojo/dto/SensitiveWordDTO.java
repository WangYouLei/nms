package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 敏感词DTO类
 * 用于前端传递敏感词数据
 */
@Data
@ApiModel("敏感词DTO")
public class SensitiveWordDTO {

    @ApiModelProperty("敏感词ID（更新时需要）")
    private Long id;

    @ApiModelProperty(value = "敏感词", required = true)
    private String word;

    @ApiModelProperty(value = "敏感词类别：1-涉政，2-涉黄，3-涉暴，4-广告，5-其他", required = true)
    private Integer category;

    @ApiModelProperty(value = "敏感等级：1-低（需人工审核），2-高（直接拒绝）", required = true)
    private Integer level;

    @ApiModelProperty(value = "状态：0-禁用，1-启用", required = true)
    private Integer status;

    @ApiModelProperty("来源：1-系统内置，2-管理员添加（新增时可不传，默认为管理员添加）")
    private Integer source;

    @ApiModelProperty("创建人ID（管理员添加时）")
    private Long creatorId;
}
