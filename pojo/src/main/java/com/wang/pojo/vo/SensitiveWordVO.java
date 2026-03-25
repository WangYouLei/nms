package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 敏感词VO类
 * 用于后端返回敏感词数据
 */
@Data
@ApiModel("敏感词VO")
public class SensitiveWordVO {

    @ApiModelProperty("敏感词ID")
    private Long id;

    @ApiModelProperty("敏感词")
    private String word;

    @ApiModelProperty("敏感词类别：1-涉政，2-涉黄，3-涉暴，4-广告，5-其他")
    private Integer category;

    @ApiModelProperty("敏感词类别名称")
    private String categoryName;

    @ApiModelProperty("敏感等级：1-低（需人工审核），2-高（直接拒绝）")
    private Integer level;

    @ApiModelProperty("敏感等级名称")
    private String levelName;

    @ApiModelProperty("状态：0-禁用，1-启用")
    private Integer status;

    @ApiModelProperty("状态名称")
    private String statusName;

    @ApiModelProperty("来源：1-系统内置，2-管理员添加")
    private Integer source;

    @ApiModelProperty("来源名称")
    private String sourceName;

    @ApiModelProperty("创建人ID")
    private Long creatorId;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}