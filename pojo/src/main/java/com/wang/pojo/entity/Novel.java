package com.wang.pojo.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
* 小说信息表
* @TableName novel
*/
@Data
public class Novel implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
    * 小说名称
    */
    @NotBlank(message="[小说名称]不能为空")
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("小说名称")
    @Length(max= 255,message="编码长度不能超过255")
    private String name;
    /**
    * 小说副名称
    */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("小说副名称")
    @Length(max= 255,message="编码长度不能超过255")
    private String subName;
    /**
    * 小说标签
    */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("小说标签")
    @Length(max= 255,message="编码长度不能超过255")
    private String tags;
    /**
    * 小说简介
    */
    @ApiModelProperty("小说简介")
    private String introduction;
    /**
    * 作者ID（关联管理人表）
    */
    @NotNull(message="[作者ID（关联管理人表）]不能为空")
    @ApiModelProperty("作者ID（关联管理人表）")
    private Integer authorId;
    /**
    * 作者名称（冗余字段）
    */
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("作者名称（冗余字段）")
    @Length(max= 20,message="编码长度不能超过20")
    private String authorName;
    /**
    * 图片路径
    */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("图片路径")
    @Length(max= 255,message="编码长度不能超过255")
    private String url;
    /**
    * 小说章节
    */
    @ApiModelProperty("小说章节")
    private Integer chapterCount;
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
    private Boolean isDel;
    /**
     * 创建时间
     */
    @NotNull(message="[创建时间]不能为空")
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    /**
    * 修改时间
    */
    @NotNull(message="[修改时间]不能为空")
    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

}