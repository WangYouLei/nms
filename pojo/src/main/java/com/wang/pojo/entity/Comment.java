package com.wang.pojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 评论表
 * @TableName comment
 */
@Data
@TableName("comment")
public class Comment implements Serializable {

    /**
     * 评论ID
     */
    @ApiModelProperty("评论ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @ApiModelProperty("用户ID")
    private Long userId;

    /**
     * 用户类型：1-访客，2-作者，3-管理员
     */
    @ApiModelProperty("用户类型：1-访客，2-作者，3-管理员")
    private Integer userType;

    /**
     * 用户昵称（冗余，避免跨表查询）
     */
    @ApiModelProperty("用户昵称（冗余，避免跨表查询）")
    private String userName;

    /**
     * 用户头像URL（冗余，避免跨表查询）
     */
    @ApiModelProperty("用户头像URL（冗余，避免跨表查询）")
    private String userAvatar;

    /**
     * 评论对象类型：1-小说，2-章节
     */
    @ApiModelProperty("评论对象类型：1-小说，2-章节")
    private Integer targetType;

    /**
     * 评论对象ID（小说ID或章节ID）
     */
    @ApiModelProperty("评论对象ID（小说ID或章节ID）")
    private Long targetId;

    /**
     * 小说ID（冗余，便于按小说查询所有评论）
     */
    @ApiModelProperty("小说ID（冗余，便于按小说查询所有评论）")
    private Long novelId;

    /**
     * 评论内容
     */
    @ApiModelProperty("评论内容")
    private String content;

    /**
     * 父评论ID（为空表示一级评论）
     */
    @ApiModelProperty("父评论ID（为空表示一级评论）")
    private Long parentId;

    /**
     * 父评论的用户ID
     */
    @ApiModelProperty("父评论的用户ID")
    private Long replyUserId;

    /**
     * 父评论的用户昵称
     */
    @ApiModelProperty("父评论的用户昵称")
    private String replyUserName;

    /**
     * 根评论ID（一级评论ID，便于查询某条评论的所有回复）
     */
    @ApiModelProperty("根评论ID（一级评论ID，便于查询某条评论的所有回复）")
    private Long rootId;

    /**
     * 已审核层级：0-未审核，1-本地过滤通过，2-人工审核通过
     */
    @ApiModelProperty("已审核层级：0-未审核，1-本地过滤通过，2-人工审核通过")
    private Integer auditLevel;

    /**
     * 回复数
     */
    @ApiModelProperty("回复数")
    private Integer replyCount;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    /**
     * 是否删除（false未删除，true已删除）
     */
    @ApiModelProperty("是否删除（false未删除，true已删除）")
    @TableLogic
    private Boolean isDel;

}
