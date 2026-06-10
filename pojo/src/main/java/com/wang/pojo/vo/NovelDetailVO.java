package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 小说详情VO类（访客端）
 * 用于返回小说详细信息
 */
@Data
@ApiModel("小说详情VO")
public class NovelDetailVO {

    @ApiModelProperty("小说ID")
    private Long id;

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

    @ApiModelProperty("作者ID")
    private Long authorId;

    @ApiModelProperty("作者名称")
    private String authorName;

    @ApiModelProperty("作者头像")
    private String authorAvatar;

    @ApiModelProperty("作者等级（1-执笔者，2-织梦师，3-造界者，4-渡舟人，5-燃灯者）")
    private Integer authorRank;

    @ApiModelProperty("作者作品数量")
    private Integer authorNovelCount;

    @ApiModelProperty("小说章节数")
    private Integer chapterCount;

    @ApiModelProperty("总字数（所有章节字数之和）")
    private Integer allWordCount;
    
    @ApiModelProperty("是否完结（false未完结，true已完结）")
    private Boolean isFinished;

    @ApiModelProperty("是否热门小说（false不是，true是）")
    private Boolean isHot;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("是否已删除（false未删除，true已删除）")
    private Boolean isDel;

    @ApiModelProperty("分类信息列表（多对多）")
    private List<NovelCategoryVO> categories;
}