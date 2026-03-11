package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说章节VO
 */
@Data
@ApiModel("小说章节VO")
public class NovelChapterVO {

    @ApiModelProperty("章节ID")
    private Integer id;

    @ApiModelProperty("小说ID")
    private Integer novelId;

    @ApiModelProperty("章节标题")
    private String title;

    @ApiModelProperty("章节内容URL")
    private String contentUrl;

    @ApiModelProperty("章节顺序")
    private Integer chapterOrder;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}