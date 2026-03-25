package com.wang.pojo.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

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
    @NotNull(message = "[评论ID]不能为空")
    @ApiModelProperty("评论ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @NotNull(message = "[用户ID]不能为空")
    @ApiModelProperty("用户ID")
    private Long userId;

    /**
     * 用户类型：1-访客，2-作者，3-管理员
     */
    @NotNull(message = "[用户类型]不能为空")
    @ApiModelProperty("用户类型：1-访客，2-作者，3-管理员")
    private Integer userType;

    /**
     * 用户昵称（冗余，避免跨表查询）
     */
    @NotBlank(message = "[用户昵称]不能为空")
    @Size(max = 50, message = "编码长度不能超过50")
    @ApiModelProperty("用户昵称（冗余，避免跨表查询）")
    @Length(max = 50, message = "编码长度不能超过50")
    private String userName;

    /**
     * 用户头像URL（冗余，避免跨表查询）
     */
    @Size(max = 255, message = "头像URL长度不能超过255")
    @ApiModelProperty("用户头像URL（冗余，避免跨表查询）")
    @Length(max = 255, message = "头像URL长度不能超过255")
    private String userAvatar;

    /**
     * 评论对象类型：1-小说，2-章节
     */
    @NotNull(message = "[评论对象类型]不能为空")
    @ApiModelProperty("评论对象类型：1-小说，2-章节")
    private Integer targetType;

    /**
     * 评论对象ID（小说ID或章节ID）
     */
    @NotNull(message = "[评论对象ID]不能为空")
    @ApiModelProperty("评论对象ID（小说ID或章节ID）")
    private Long targetId;

    /**
     * 小说ID（冗余，便于按小说查询所有评论）
     */
    @NotNull(message = "[小说ID]不能为空")
    @ApiModelProperty("小说ID（冗余，便于按小说查询所有评论）")
    private Long novelId;

    /**
     * 评论内容
     */
    @NotBlank(message = "[评论内容]不能为空")
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
    @Size(max = 50, message = "编码长度不能超过50")
    @ApiModelProperty("父评论的用户昵称")
    @Length(max = 50, message = "编码长度不能超过50")
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
    @NotNull(message = "[回复数]不能为空")
    @ApiModelProperty("回复数")
    private Integer replyCount;

    /**
     * 创建时间
     */
    @NotNull(message = "[创建时间]不能为空")
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @NotNull(message = "[更新时间]不能为空")
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

}