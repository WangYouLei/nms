package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 小说分类关联DTO
 * 用于批量设置小说的分类（多对多关系）
 */
@Data
@ApiModel("小说分类关联DTO")
public class NovelCategoryRelationDTO {

    @ApiModelProperty("小说ID")
    private Integer novelId;

    @ApiModelProperty("分类ID列表（支持批量设置多个分类）")
    private List<Integer> categoryIds;

}