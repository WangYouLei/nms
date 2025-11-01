package com.wang.pojo.entity;

import javax.validation.constraints.NotNull;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
* 地点与势力关联表
* @TableName location_faction
*/
@Data
public class LocationFaction implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    private Integer id;
    /**
    * 地点ID（关联地图表）
    */
    @NotNull(message="[地点ID（关联地图表）]不能为空")
    @ApiModelProperty("地点ID（关联地图表）")
    private Integer locationId;
    /**
    * 势力ID（关联势力表）
    */
    @NotNull(message="[势力ID（关联势力表）]不能为空")
    @ApiModelProperty("势力ID（关联势力表）")
    private Integer factionId;

}
