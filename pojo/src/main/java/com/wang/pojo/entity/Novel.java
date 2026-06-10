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
* 小说信息表
* @TableName novel
*/
@Data
@TableName("novel")
public class Novel implements Serializable {

    /**
    * 主键ID
    */
    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
    * 小说名称
    */
    @ApiModelProperty("小说名称")
    private String name;
    /**
    * 小说副名称
    */
    @ApiModelProperty("小说副名称")
    private String subName;
    /**
    * 小说标签
    */
    @ApiModelProperty("小说标签")
    private String tags;
    /**
    * 小说简介
    */
    @ApiModelProperty("小说简介")
    private String introduction;
    /**
    * 作者ID（关联管理人表）
    */
    @ApiModelProperty("作者ID（关联管理人表）")
    private Long authorId;
/**
     * 作者名称（冗余字段）
     */
    @ApiModelProperty("作者名称（冗余字段）")
    private String authorName;
    /**
     * 作者头像地址（冗余字段）
     */
    @ApiModelProperty("作者头像地址（冗余字段）")
    private String authorAvatar;
    /**
     * 作者等级（1-执笔者，2-织梦师，3-造界者，4-渡舟人，5-燃灯者）
     */
    @ApiModelProperty("作者等级（1-执笔者，2-织梦师，3-造界者，4-渡舟人，5-燃灯者）")
    private Integer authorRank;
    /**
     * 图片路径
     */
    @ApiModelProperty("图片路径")
    private String url;
/**
     * 小说章节
     */
    @ApiModelProperty("小说章节")
    private Integer chapterCount;
    /**
     * 总字数（所有章节字数之和）
     */
    @ApiModelProperty("总字数（所有章节字数之和）")
    private Integer allWordCount;
    /**
     * 收藏数量
     */
    @ApiModelProperty("收藏数量")
    private Integer collectCount;
    /**
     * 是否完结（false未完结，true已完结）
     */
    @ApiModelProperty("是否完结（false未完结，true已完结）")
    private Boolean isFinished;
    /**
     * 是否热门小说（false不是，true是）
     */
    @ApiModelProperty("是否热门小说（false不是，true是）")
    private Boolean isHot;
    /**
     * 是否删除（false未删除，true已删除）
     */
    @ApiModelProperty("是否删除（false未删除，true已删除）")
    @TableLogic
    private Boolean isDel;
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
