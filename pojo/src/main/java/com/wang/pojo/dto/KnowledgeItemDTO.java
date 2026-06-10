package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("知识项请求")
public class KnowledgeItemDTO {

    @ApiModelProperty("知识项ID（更新时必填）")
    private Long id;

    @ApiModelProperty("小说ID")
    private Long novelId;

    @ApiModelProperty("知识项类型：character/setting/plot/theme/item")
    private String itemType;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("内容（JSON格式）")
    private String content;

    @ApiModelProperty("摘要")
    private String summary;

    @ApiModelProperty("来源章节ID")
    private Long sourceChapterId;

    @ApiModelProperty("来源章节序号")
    private Integer sourceChapterOrder;

    @ApiModelProperty("置信度 0.0-1.0")
    private Double confidence;

    @ApiModelProperty("状态：-1=拒绝，0=待确认，1=已确认，2=已修改")
    private Integer status;
}
