package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("文本润色请求")
public class PolishRequestDTO {

    @ApiModelProperty("待润色文本")
    private String text;

    @ApiModelProperty("润色维度：grammar, style, coherence, description, dialogue, custom")
    private List<String> aspects;

    @ApiModelProperty("自定义指令")
    private String customInstruction;

    @ApiModelProperty("是否保持原文长度，默认 true")
    private Boolean preserveLength;

    @ApiModelProperty("是否生成更长版本")
    private Boolean generateLonger;

    @ApiModelProperty("温度 0.0-1.0，默认 0.3")
    private Double temperature;

    @ApiModelProperty("小说ID（可选，用于获取写作风格）")
    private Long novelId;
}
