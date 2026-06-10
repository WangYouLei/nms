package com.wang.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("visitor_reading_progress")
public class VisitorReadingProgress implements Serializable {

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("访客ID")
    private Long visitorId;

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

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
