package com.wang.pojo.dto;

import lombok.Data;

/**
 * 访客关注作者DTO
 */
@Data
public class VisitorFollowDTO {
    /**
     * 访客ID
     */
    private Integer visitorId;

    /**
     * 作者ID
     */
    private Integer authorId;

    /**
     * 作者名称
     */
    private String authorName;

    /**
     * 作者头像URL
     */
    private String authorAvatar;

    /**
     * 作者等级（1-执笔者，2-织梦师，3-造界者，4-渡舟人，5-燃灯者）
     */
    private Integer authorRank;
}