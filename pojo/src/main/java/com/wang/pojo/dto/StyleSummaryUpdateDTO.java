package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("写作风格更新请求")
public class StyleSummaryUpdateDTO {

    @ApiModelProperty("小说ID")
    private Long novelId;

    @ApiModelProperty("风格文本")
    private String styleText;
}
