package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访客关注作者VO
 */
@Data
@ApiModel("访客关注作者VO")
public class VisitorFollowVO {

    @ApiModelProperty("关注记录ID")
    private Integer id;

    @ApiModelProperty("访客ID")
    private Integer visitorId;

    @ApiModelProperty("作者ID")
    private Integer authorId;

    @ApiModelProperty("作者名称")
    private String authorName;

    @ApiModelProperty("作者头像URL")
    private String authorAvatar;

    @ApiModelProperty("作者等级（1-执笔者，2-织梦师，3-造界者，4-渡舟人，5-燃灯者）")
    private Integer authorRank;

    @ApiModelProperty("作者等级名称")
    private String authorRankName;

    @ApiModelProperty("作者作品数量")
    private Integer novelCount;

    @ApiModelProperty("作者简介")
    private String authorIntroduction;

    @ApiModelProperty("关注时间")
    private LocalDateTime createTime;
}