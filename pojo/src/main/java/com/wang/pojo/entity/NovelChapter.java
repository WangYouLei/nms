package com.wang.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小说章节表
 * @TableName novel_chapter
 */
@Data
@TableName("novel_chapter")
public class NovelChapter implements Serializable {

    /**
     * 主键ID
     */
    @NotNull(message = "[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 小说ID
     */
    @NotNull(message = "[小说ID]不能为空")
    @ApiModelProperty("小说ID")
    private Integer novelId;

    /**
     * 章节标题
     */
    @NotBlank(message = "[章节标题]不能为空")
    @ApiModelProperty("章节标题")
    private String title;

    /**
     * 章节内容URL（MinIO存储路径）
     */
    @NotBlank(message = "[章节内容URL]不能为空")
    @ApiModelProperty("章节内容URL")
    private String contentUrl;

    /**
     * 章节字数
     */
    @ApiModelProperty("章节字数")
    private Integer wordCount;

    /**
     * 章节顺序
     */
    @NotNull(message = "[章节顺序]不能为空")
    @ApiModelProperty("章节顺序")
    private Integer chapterOrder;

    /**
     * 创建时间
     */
    @NotNull(message = "[创建时间]不能为空")
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @NotNull(message = "[修改时间]不能为空")
    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;
}