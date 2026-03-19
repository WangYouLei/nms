package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 作者统计VO
 */
@Data
@ApiModel("作者统计VO")
public class AuthorStatisticsVO implements Serializable {

    @ApiModelProperty("分组数据列表")
    private List<Item> items;

    @Data
    @ApiModel("统计项")
    public static class Item implements Serializable {

        @ApiModelProperty("等级值")
        private Integer rank;

        @ApiModelProperty("等级名称")
        private String rankName;

        @ApiModelProperty("数量")
        private Long count;
    }
}