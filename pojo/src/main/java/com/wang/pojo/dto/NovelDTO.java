package com.wang.pojo.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class NovelDTO implements Serializable {
    /**
     * 主键ID
     */

    private Integer id;
    /**
     * 小说名称
     */

    private String name;
    /**
     * 小说副名称
     */

    private String subName;

    /**
     * 小说标签
     */
    private String tags;

    /**
     * 小说简介
     */
    private String introduction;

    /**
     * 作者名称（冗余字段）
     */
    private String authorName;

    /**
     * 图片路径
     */
    private String url;

    /**
     * 小说章节
     */
    private Integer chapterCount;

    /**
     * 是否完结（false未完结，true已完结）
     */
    private Boolean isFinished;
    /**
     * 是否热门小说（false不是，true是）
     */
    private Boolean isHot;
}