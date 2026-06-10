package com.wang.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 小说ID
     */
    @ApiModelProperty("小说ID")
    private Long novelId;

    /**
     * 章节标题
     */
    @ApiModelProperty("章节标题")
    private String title;

    /**
     * 章节内容URL（MinIO存储路径）
     */
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
    @ApiModelProperty("章节顺序")
    private Integer chapterOrder;

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
