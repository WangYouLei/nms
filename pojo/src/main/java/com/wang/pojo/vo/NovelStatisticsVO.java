package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 小说统计VO
 */
@Data
@ApiModel("小说统计VO")
public class NovelStatisticsVO implements Serializable {

    @ApiModelProperty("分组数据列表")
    private List<Item> items;

    @Data
    @ApiModel("统计项")
    public static class Item implements Serializable {
        
        @ApiModelProperty("名称")
        private String name;

        @ApiModelProperty("数量")
        private Long count;
    }
}