package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("AI写作助手请求DTO")
public class AiWritingDTO {

    @ApiModelProperty("功能类型：1-续写建议，2-章节摘要，3-角色一致性检查，4-标题/简介优化")
    private Integer type;

    @ApiModelProperty("当前章节内容")
    private String content;

    @ApiModelProperty("前文上下文（前几章内容摘要或全文，用于续写和角色检查）")
    private String context;

    @ApiModelProperty("小说标题（用于标题优化）")
    private String title;

    @ApiModelProperty("小说简介（用于简介优化）")
    private String introduction;
}
