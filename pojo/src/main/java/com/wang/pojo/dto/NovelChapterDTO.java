package com.wang.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 小说章节DTO
 */
@Data
public class NovelChapterDTO implements Serializable {

    /**
     * 主键ID
     */
    @ApiModelProperty("主键ID")
    private Integer id;

    /**
     * 小说ID
     */
    @ApiModelProperty("小说ID")
    private Integer novelId;

    /**
     * 章节标题
     */
    @ApiModelProperty("章节标题")
    private String title;

    /**
     * 章节内容
     */
    @ApiModelProperty("章节内容")
    private String content;

    /**
     * 章节顺序
     */
    @ApiModelProperty("章节顺序")
    private Integer chapterOrder;
}