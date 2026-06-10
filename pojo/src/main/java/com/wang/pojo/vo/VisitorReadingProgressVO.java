package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("阅读进度VO")
public class VisitorReadingProgressVO {

    @ApiModelProperty("记录ID")
    private Long id;

    @ApiModelProperty("小说ID")
    private Long novelId;

    @ApiModelProperty("小说名称")
    private String novelName;

    @ApiModelProperty("小说封面地址")
    private String novelUrl;

    @ApiModelProperty("作者名称")
    private String authorName;

    @ApiModelProperty("当前阅读章节ID")
    private Long chapterId;

    @ApiModelProperty("当前阅读章节序号")
    private Integer chapterOrder;

    @ApiModelProperty("最近阅读时间")
    private LocalDateTime lastReadTime;

    @ApiModelProperty("首次阅读时间")
    private LocalDateTime createTime;
}
