package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
* 小说分类VO
*/
@Data
@ApiModel("小说分类VO")
public class NovelCategoryVO {

    @ApiModelProperty("分类ID")
    private Long id;

    @ApiModelProperty("分类类型")
    private String type;

    @ApiModelProperty("频道：1-男频,2-女频")
    private Integer category;

    @ApiModelProperty("频道名称")
    private String categoryName;

    @ApiModelProperty("是否是热门标签：0-否,1-是")
    private Integer isHot;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

}