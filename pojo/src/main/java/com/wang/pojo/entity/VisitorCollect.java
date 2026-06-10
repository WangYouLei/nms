package com.wang.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小说收藏表
 * @TableName visitor_collect
 */
@Data
@TableName("visitor_collect")
public class VisitorCollect implements Serializable {

    /**
     * 主键ID
     */
    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（关联visitor表）
     */
    @ApiModelProperty("用户ID")
    private Long visitorId;

    /**
     * 小说ID（关联novel表）
     */
    @ApiModelProperty("小说ID")
    private Long novelId;

    /**
     * 小说名称（冗余字段）
     */
    @ApiModelProperty("小说名称")
    private String novelName;

    /**
     * 小说封面地址（冗余字段）
     */
    @ApiModelProperty("小说封面地址")
    private String novelUrl;

    /**
     * 作者名称（冗余字段）
     */
    @ApiModelProperty("作者名称")
    private String authorName;

    /**
     * 作者头像（冗余字段）
     */
    @ApiModelProperty("作者头像")
    private String authorAvatar;

    /**
     * 作者等级（冗余字段）
     */
    @ApiModelProperty("作者等级")
    private Integer authorRank;

    /**
     * 收藏时间
     */
    @ApiModelProperty("收藏时间")
    private LocalDateTime createTime;
}