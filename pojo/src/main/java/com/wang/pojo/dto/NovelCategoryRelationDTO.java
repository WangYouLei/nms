package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;



/**
* 小说分类关联DTO
*/
@Data
@ApiModel("小说分类关联DTO")
public class NovelCategoryRelationDTO {

    @ApiModelProperty("关联ID")
    private Integer id;

    @ApiModelProperty("小说ID")
    private Integer novelId;

    @ApiModelProperty("分类ID")
    private Integer categoryId;

}