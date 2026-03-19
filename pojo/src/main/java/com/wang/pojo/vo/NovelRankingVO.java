package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 小说排行榜VO
 */
@Data
@ApiModel("小说排行榜VO")
public class NovelRankingVO implements Serializable {

    @ApiModelProperty("排行榜数据列表")
    private List<Item> items;

    @Data
    @ApiModel("排行榜项")
    public static class Item implements Serializable {

        @ApiModelProperty("排名")
        private Integer rank;

        @ApiModelProperty("小说ID")
        private Integer id;

        @ApiModelProperty("小说名称")
        private String name;

        @ApiModelProperty("作者名称")
        private String authorName;

        @ApiModelProperty("章节数")
        private Integer chapterCount;

        @ApiModelProperty("是否完结")
        private Boolean isFinished;

        @ApiModelProperty("是否热门")
        private Boolean isHot;

        @ApiModelProperty("封面URL")
        private String url;

        @ApiModelProperty("更新时间")
        private String updateTime;
    }
}