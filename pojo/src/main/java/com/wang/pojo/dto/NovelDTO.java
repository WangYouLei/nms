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
     * 作者头像地址（冗余字段）
     */
    private String authorAvatar;

    /**
     * 作者等级（1-执笔者，2-织梦师，3-造界者，4-渡舟人，5-燃灯者）
     */
    private Integer authorRank;

    /**
     * 作者ID（关联管理人表）
     */
    private Integer authorId;

    /**
     * 图片路径
     */
    private String url;

    /**
     * 小说章节数
     */
    private Integer chapterCount;

    /**
     * 是否完结（false未完结，true已完结）
     */
    private Boolean isFinished;
}