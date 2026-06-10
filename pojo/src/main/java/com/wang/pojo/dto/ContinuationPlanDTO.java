package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("续写大纲规划请求")
public class ContinuationPlanDTO {

    @ApiModelProperty("小说ID")
    private Long novelId;

    @ApiModelProperty("当前章节内容")
    private String currentContent;

    @ApiModelProperty("最近章节摘要列表（最多5条）")
    private List<String> chapterSummaries;

    @ApiModelProperty("作者自定义指令")
    private String authorInstructions;
}
