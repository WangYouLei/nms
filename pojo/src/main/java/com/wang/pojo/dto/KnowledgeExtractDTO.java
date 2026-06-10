package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("知识提取请求")
public class KnowledgeExtractDTO {

    @ApiModelProperty("小说ID")
    private Long novelId;

    @ApiModelProperty("章节ID")
    private Long chapterId;

    @ApiModelProperty("章节序号")
    private Integer chapterOrder;

    @ApiModelProperty("章节文本内容")
    private String chapterText;

    @ApiModelProperty("小说基础信息（可选）")
    private String novelInfo;
}
