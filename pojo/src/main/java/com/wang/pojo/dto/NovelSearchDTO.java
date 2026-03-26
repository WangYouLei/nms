package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("小说搜索DTO")
public class NovelSearchDTO {

    @ApiModelProperty("搜索关键词（模糊匹配名称、副名称、标签）")
    private String keyword;

    @ApiModelProperty("小说名称")
    private String name;

    @ApiModelProperty("小说副名称")
    private String subName;

    @ApiModelProperty("作者ID")
    private Integer authorId;

    @ApiModelProperty("是否删除")
    private Boolean isDel;

    @ApiModelProperty("是否热门")
    private Boolean isHot;

    @ApiModelProperty("是否完结")
    private Boolean isFinished;

    @ApiModelProperty("排序方式：update-最新更新，collect-收藏最多，word-字数最多")
    private String sortBy;

    @ApiModelProperty("页码")
    private Integer pageNum;

    @ApiModelProperty("每页数量")
    private Integer pageSize;
}
