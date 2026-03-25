package com.wang.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 评论DTO类
 * 用于前端传递评论数据
 */
@Data
@ApiModel("评论DTO")
public class CommentDTO {

    @ApiModelProperty("评论ID（更新时需要）")
    private Long id;

    @NotNull(message = "[用户ID]不能为空")
    @ApiModelProperty(value = "用户ID", required = true)
    private Long userId;

    @NotNull(message = "[用户类型]不能为空")
    @ApiModelProperty(value = "用户类型：1-访客，2-作者，3-管理员", required = true)
    private Integer userType;

    @NotBlank(message = "[用户昵称]不能为空")
    @Size(max = 50, message = "用户昵称长度不能超过50")
    @ApiModelProperty(value = "用户昵称", required = true)
    private String userName;

    @Size(max = 255, message = "头像URL长度不能超过255")
    @ApiModelProperty(value = "用户头像URL")
    private String userAvatar;

    @NotNull(message = "[评论对象类型]不能为空")
    @ApiModelProperty(value = "评论对象类型：1-小说，2-章节", required = true)
    private Integer targetType;

    @NotNull(message = "[评论对象ID]不能为空")
    @ApiModelProperty(value = "评论对象ID（小说ID或章节ID）", required = true)
    private Long targetId;

    @NotNull(message = "[小说ID]不能为空")
    @ApiModelProperty(value = "小说ID", required = true)
    private Long novelId;

    @NotBlank(message = "[评论内容]不能为空")
    @ApiModelProperty(value = "评论内容", required = true)
    private String content;

    @ApiModelProperty("父评论ID（为空表示一级评论）")
    private Long parentId;

    @ApiModelProperty("父评论的用户ID")
    private Long replyUserId;

    @Size(max = 50, message = "父评论用户昵称长度不能超过50")
    @ApiModelProperty("父评论的用户昵称")
    private String replyUserName;

    @ApiModelProperty("根评论ID（一级评论ID，便于查询某条评论的所有回复）")
    private Long rootId;
}