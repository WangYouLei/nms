package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 作者排行榜VO
 */
@Data
@ApiModel("作者排行榜VO")
public class AuthorRankingVO implements Serializable {

    @ApiModelProperty("排行榜数据列表")
    private List<Item> items;

    @Data
    @ApiModel("排行榜项")
    public static class Item implements Serializable {

        @ApiModelProperty("排名")
        private Integer rank;

        @ApiModelProperty("作者ID")
        private Long id;

        @ApiModelProperty("作者名称")
        private String name;

        @ApiModelProperty("作者等级")
        private Integer authorRank;

        @ApiModelProperty("等级名称")
        private String rankName;

        @ApiModelProperty("作品数量")
        private Integer novelCount;

        @ApiModelProperty("头像URL")
        private String avatar;
    }
}