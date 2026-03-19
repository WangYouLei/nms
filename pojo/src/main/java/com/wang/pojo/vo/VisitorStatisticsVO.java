package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户统计VO
 */
@Data
@ApiModel("用户统计VO")
public class VisitorStatisticsVO implements Serializable {

    @ApiModelProperty("分组数据列表")
    private List<Item> items;

    @Data
    @ApiModel("统计项")
    public static class Item implements Serializable {

        @ApiModelProperty("VIP等级值")
        private Integer vipLevel;

        @ApiModelProperty("VIP等级名称")
        private String vipName;

        @ApiModelProperty("数量")
        private Long count;
    }
}