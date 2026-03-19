package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 趋势统计VO
 */
@Data
@ApiModel("趋势统计VO")
public class TrendVO implements Serializable {

    @ApiModelProperty("趋势数据列表")
    private List<Item> items;

    @Data
    @ApiModel("趋势项")
    public static class Item implements Serializable {

        @ApiModelProperty("日期（格式根据粒度：yyyy-MM-dd / yyyy-ww / yyyy-MM / yyyy）")
        private String date;

        @ApiModelProperty("数量")
        private Long count;
    }
}