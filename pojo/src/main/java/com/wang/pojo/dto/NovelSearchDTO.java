package com.wang.pojo.dto;

import lombok.Data;

@Data
public class NovelSearchDTO {

    /**
     * 小说名称
     */
    private String name;

    /**
     * 小说副名称
     */
    private String subName;

    /**
     * 是否删除
     */
    private Boolean ifDel;

    /**
     * 是否热门
     */
    private Boolean isHot;

    /**
     * 是否完结
     */
    private Boolean isFinished;

    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;
}
