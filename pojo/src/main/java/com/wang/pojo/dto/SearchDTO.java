package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("统一搜索DTO")
public class SearchDTO {

    @ApiModelProperty("搜索关键词（全文搜索名称、标签、简介、作者名、分类名）")
    private String keyword;

    @ApiModelProperty("小说名称")
    private String name;

    @ApiModelProperty("小说副名称")
    private String subName;

    @ApiModelProperty("作者ID")
    private Long authorId;

    @ApiModelProperty("是否热门")
    private Boolean isHot;

    @ApiModelProperty("是否完结")
    private Boolean isFinished;

    @ApiModelProperty("分类ID（按分类过滤）")
    private Long categoryId;

    @ApiModelProperty("频道类型（1男频/2女频）")
    private Integer categoryType;

    @ApiModelProperty("标签（按标签搜索）")
    private String tag;

    @ApiModelProperty("排序方式：update-最新更新，collect-收藏最多，word-字数最多，relevance-相关度")
    private String sortBy;

    @ApiModelProperty("页码")
    private Integer pageNum;

    @ApiModelProperty("每页数量")
    private Integer pageSize;
}
