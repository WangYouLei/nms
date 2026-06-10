package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("续写生成请求")
public class ContinuationGenerateDTO {

    @ApiModelProperty("小说ID")
    private Long novelId;

    @ApiModelProperty("当前章节内容")
    private String currentContent;

    @ApiModelProperty("最近章节摘要列表（最多5条）")
    private List<String> chapterSummaries;

    @ApiModelProperty("作者自定义指令")
    private String authorInstructions;

    @ApiModelProperty("选中的大纲索引，0-based")
    private Integer selectedOutlineIndex;

    @ApiModelProperty("选中的大纲文本")
    private String selectedOutline;

    @ApiModelProperty("生成温度 0.0-2.0，默认 0.7")
    private Double temperature;

    @ApiModelProperty("最大生成 token 数，默认 2048")
    private Integer maxTokens;
}
