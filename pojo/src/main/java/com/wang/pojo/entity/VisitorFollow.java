package com.wang.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 访客关注作者表
 * @TableName visitor_follow
 */
@Data
@TableName("visitor_follow")
public class VisitorFollow implements Serializable {

    /**
     * 主键ID
     */
    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 访客ID
     */
    @ApiModelProperty("访客ID")
    private Integer visitorId;

    /**
     * 作者ID
     */
    @ApiModelProperty("作者ID")
    private Integer authorId;

    /**
     * 作者名称（冗余）
     */
    @ApiModelProperty("作者名称")
    private String authorName;

    /**
     * 作者头像URL（冗余）
     */
    @ApiModelProperty("作者头像URL")
    private String authorAvatar;

    /**
     * 作者等级（1-执笔者，2-织梦师，3-造界者，4-渡舟人，5-燃灯者）
     */
    @ApiModelProperty("作者等级（1-执笔者，2-织梦师，3-造界者，4-渡舟人，5-燃灯者）")
    private Integer authorRank;

    /**
     * 关注时间
     */
    @ApiModelProperty("关注时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}