package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说详情VO类（访客端）
 * 用于返回小说详细信息
 */
@Data
@ApiModel("小说详情VO")
public class NovelDetailVO {

    @ApiModelProperty("小说ID")
    private Integer id;

    @ApiModelProperty("小说名称")
    private String name;

    @ApiModelProperty("小说副名称")
    private String subName;

    @ApiModelProperty("小说标签")
    private String tags;

    @ApiModelProperty("小说简介")
    private String introduction;

    @ApiModelProperty("封面图片")
    private String url;

    @ApiModelProperty("作者名称")
    private String authorName;

    @ApiModelProperty("小说章节")
    private Integer chapterCount;
    
    @ApiModelProperty("是否完结（false未完结，true已完结）")
    private Boolean isFinished;

    @ApiModelProperty("是否热门小说（false不是，true是）")
    private Boolean isHot;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("分类信息")
    private NovelCategoryVO category;
}