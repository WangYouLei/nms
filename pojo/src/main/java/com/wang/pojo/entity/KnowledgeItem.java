package com.wang.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识项实体 - 小说知识图谱
 */
@Data
@TableName("knowledge_items")
public class KnowledgeItem implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("知识项ID")
    private Long id;

    @ApiModelProperty("小说ID")
    private Long novelId;

    @ApiModelProperty("知识项类型：character/setting/plot/theme/item/style_summary")
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

    @ApiModelProperty("版本号")
    private Integer version;

    @ApiModelProperty("状态：-1=拒绝，0=待确认，1=已确认，2=已修改")
    private Integer status;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
}
