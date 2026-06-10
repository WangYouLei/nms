package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("写作风格刷新请求")
public class StyleSummaryRefreshDTO {

    @ApiModelProperty("小说ID")
    private Long novelId;

    @ApiModelProperty("章节样本文本")
    private String chapterSamples;
}
