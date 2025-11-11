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
     * 图片路径
     */
    private String url;
}
