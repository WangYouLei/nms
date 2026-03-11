package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
* 小说分类DTO
*/
@Data
@ApiModel("小说分类DTO")
public class NovelCategoryDTO {

    @ApiModelProperty("分类ID")
    private Integer id;


    @ApiModelProperty("分类类型")
    private String type;


    @ApiModelProperty("频道：1-男频,2-女频")
    private Integer category;

    @ApiModelProperty("是否是热门标签：0-否,1-是")
    private Integer isHot;

}