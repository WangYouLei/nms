package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论VO类
 * 用于后端返回评论数据
 */
@Data
@ApiModel("评论VO")
public class CommentVO {

    @ApiModelProperty("评论ID")
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户类型：1-访客，2-作者，3-管理员")
    private Integer userType;

    @ApiModelProperty("用户类型名称")
    private String userTypeName;

    @ApiModelProperty("用户昵称")
    private String userName;

    @ApiModelProperty("用户头像URL")
    private String userAvatar;

    @ApiModelProperty("评论对象类型：1-小说，2-章节")
    private Integer targetType;

    @ApiModelProperty("评论对象类型名称")
    private String targetTypeName;

    @ApiModelProperty("评论对象ID（小说ID或章节ID）")
    private Long targetId;

    @ApiModelProperty("小说ID")
    private Long novelId;

    @ApiModelProperty("小说作者ID（用于判断评论者是否是该小说的作者）")
    private Long novelAuthorId;

    @ApiModelProperty("评论内容")
    private String content;

    @ApiModelProperty("父评论ID（为空表示一级评论）")
    private Long parentId;

    @ApiModelProperty("父评论的用户ID")
    private Long replyUserId;

    @ApiModelProperty("父评论的用户昵称")
    private String replyUserName;

    @ApiModelProperty("根评论ID")
    private Long rootId;

    @ApiModelProperty("审核层级：0-未审核，1-本地过滤通过，2-人工审核通过")
    private Integer auditLevel;

    @ApiModelProperty("审核层级名称")
    private String auditLevelName;

    @ApiModelProperty("回复数")
    private Integer replyCount;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("子评论列表（回复）")
    private List<CommentVO> replies;
}