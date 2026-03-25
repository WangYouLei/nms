package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 敏感词查询DTO类
 * 用于查询敏感词列表
 */
@Data
@ApiModel("敏感词查询DTO")
public class SensitiveWordQueryDTO {

    @ApiModelProperty("敏感词（模糊查询）")
    private String word;

    @ApiModelProperty("敏感词类别：1-涉政，2-涉黄，3-涉暴，4-广告，5-其他")
    private Integer category;

    @ApiModelProperty("敏感等级：1-低，2-高")
    private Integer level;

    @ApiModelProperty("状态：0-禁用，1-启用")
    private Integer status;

    @ApiModelProperty("来源：1-系统内置，2-管理员添加")
    private Integer source;

    @ApiModelProperty("页码（默认1）")
    private Integer pageNum = 1;

    @ApiModelProperty("每页数量（默认10）")
    private Integer pageSize = 10;
}