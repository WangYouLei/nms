package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 作者详情VO（访客端）
 * 用于作者详情页，包含作者基本信息和作品列表
 */
@Data
@ApiModel("作者详情VO（访客端）")
public class AuthorDetailVO implements Serializable {

    @ApiModelProperty("作者ID")
    private Integer id;

    @ApiModelProperty("作者昵称")
    private String name;

    @ApiModelProperty("作者头像")
    private String avatar;

    @ApiModelProperty("等级：1-执笔者，2-织梦师，3-造界者，4-渡舟人，5-燃灯者")
    private Integer rank;

    @ApiModelProperty("等级名称")
    private String rankName;

    @ApiModelProperty("作者简介")
    private String introduction;

    @ApiModelProperty("作品数量")
    private Integer novelCount;

    @ApiModelProperty("作者的作品列表")
    private List<NovelListVO> novels;
}