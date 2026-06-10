package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访客VO类
 * 用于后端返回访客数据
 */
@Data
@ApiModel("访客VO")
public class VisitorVO {

    @ApiModelProperty("访客ID")
    private Long id;

    @ApiModelProperty("访客名称")
    private String name;

    @ApiModelProperty("头像地址")
    private String avatar;

    @ApiModelProperty("账号")
    private String account;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("VIP级别：0-普通,1-VIP1,2-VIP2,3-VIP3,4-金主")
    private Integer vipLevel;

    @ApiModelProperty("VIP级别名称")
    private String vipLevelName;

    @ApiModelProperty("是否删除：false-否，true-是")
    private Boolean isDel;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}