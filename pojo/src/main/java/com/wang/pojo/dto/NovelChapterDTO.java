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
     * 章节顺序
     */
    @ApiModelProperty("章节顺序")
    private Integer chapterOrder;
}