package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("续写大纲规划响应")
public class ContinuationPlanVO {

    @ApiModelProperty("续写方向大纲列表")
    private List<String> outlines;
}
