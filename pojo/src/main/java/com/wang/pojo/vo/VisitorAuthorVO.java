package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 作者信息VO（访客端）
 * 只包含访客可见的信息，不包含敏感信息
 */
@Data
@ApiModel("作者信息VO（访客端）")
public class VisitorAuthorVO implements Serializable {

    @ApiModelProperty("作者ID")
    private Integer id;

    @ApiModelProperty("作者昵称")
    private String name;

    @ApiModelProperty("作者头像")
    private String avatar;

    @ApiModelProperty("等级：1-执笔者，2-织梦师，3-造界者，4-渡舟人，5-燃灯者")
    private Integer rank;

    @ApiModelProperty("作者简介")
    private String introduction;

    @ApiModelProperty("作品数量")
    private Integer novelCount;
}