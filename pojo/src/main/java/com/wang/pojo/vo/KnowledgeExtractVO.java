package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("知识提取响应")
public class KnowledgeExtractVO {

    @ApiModelProperty("提取的实体总数")
    private Integer extractedCount;

    @ApiModelProperty("新增的知识项")
    private List<ItemSummary> newItems;

    @ApiModelProperty("更新的知识项")
    private List<ItemSummary> updatedItems;

    @Data
    public static class ItemSummary {
        private Long id;
        private String itemType;
        private String name;
        private String summary;
    }
}
