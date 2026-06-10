package com.wang.pojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
* 小说分类表
* @TableName novel_category
*/
@Data
@TableName("novel_category")
public class NovelCategory implements Serializable {

    /**
    * 主键ID
    */
    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
    * 分类类型
    */
    @ApiModelProperty("分类类型")
    private String type;

    /**
    * 频道：1-男频,2-女频
    */
    @ApiModelProperty("频道：1-男频,2-女频")
    private Integer category;

    /**
    * 是否是热门标签：0-否,1-是
    */
    @ApiModelProperty("是否是热门标签：0-否,1-是")
    private Integer isHot;

    /**
    * 创建时间
    */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
    * 修改时间
    */
    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

}
