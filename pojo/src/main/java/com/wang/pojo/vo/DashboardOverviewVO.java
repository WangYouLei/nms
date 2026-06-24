package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据概览VO
 */
@Data
@ApiModel("数据概览VO")
public class DashboardOverviewVO implements Serializable {

    @ApiModelProperty("小说总数")
    private Long novelCount;

    @ApiModelProperty("作者总数")
    private Long authorCount;

    @ApiModelProperty("用户总数")
    private Long visitorCount;

    @ApiModelProperty("分类总数")
    private Long categoryCount;

    @ApiModelProperty("今日新增小说")
    private Long todayNewNovels;

    @ApiModelProperty("今日新增作者")
    private Long todayNewAuthors;

    @ApiModelProperty("今日新增用户")
    private Long todayNewVisitors;

    @ApiModelProperty("本周新增小说")
    private Long weekNewNovels;

    @ApiModelProperty("本周新增作者")
    private Long weekNewAuthors;

    @ApiModelProperty("本周新增用户")
    private Long weekNewVisitors;

    @ApiModelProperty("本月新增小说")
    private Long monthNewNovels;

    @ApiModelProperty("本月新增作者")
    private Long monthNewAuthors;

    @ApiModelProperty("本月新增用户")
    private Long monthNewVisitors;

    @ApiModelProperty("热门小说数")
    private Long hotNovelCount;

    @ApiModelProperty("完结小说数")
    private Long finishedNovelCount;
}