package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏小说VO
 */
@Data
@ApiModel("收藏小说VO")
public class VisitorCollectVO {

    @ApiModelProperty("收藏记录ID")
    private Integer id;

    @ApiModelProperty("小说ID")
    private Integer novelId;

    @ApiModelProperty("小说名称")
    private String novelName;

    @ApiModelProperty("小说封面地址")
    private String novelUrl;

    @ApiModelProperty("作者名称")
    private String authorName;

    @ApiModelProperty("作者头像")
    private String authorAvatar;

    @ApiModelProperty("作者等级")
    private Integer authorRank;

    @ApiModelProperty("收藏时间")
    private LocalDateTime createTime;
}