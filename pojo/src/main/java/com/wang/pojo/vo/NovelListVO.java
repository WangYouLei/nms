package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说列表VO类（访客端）
 * 用于返回小说列表信息
 */
@Data
@ApiModel("小说列表VO")
public class NovelListVO {

    @ApiModelProperty("小说ID")
    private Integer id;

    @ApiModelProperty("小说名称")
    private String name;

    @ApiModelProperty("小说副名称")
    private String subName;

    @ApiModelProperty("小说标签")
    private String tags;

    @ApiModelProperty("封面图片")
    private String url;

    @ApiModelProperty("作者名称")
    private String authorName;

    @ApiModelProperty("作者头像")
    private String authorAvatar;

    @ApiModelProperty("小说章节")
    private Integer chapterCount;
    
    @ApiModelProperty("是否完结（false未完结，true已完结）")
    private Boolean isFinished;

    @ApiModelProperty("是否热门小说（false不是，true是）")
    private Boolean isHot;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("分类名称")
    private String categoryName;
}