package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("知识项响应")
public class KnowledgeItemVO {

    private Long id;
    private Long novelId;
    private String itemType;
    private String name;
    private String content;
    private String summary;
    private Long sourceChapterId;
    private Integer sourceChapterOrder;
    private Double confidence;
    private Integer version;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
