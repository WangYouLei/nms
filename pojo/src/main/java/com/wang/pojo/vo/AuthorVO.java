package com.wang.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 作者信息VO
 *
 * @TableName author
 */
@Data
public class AuthorVO implements Serializable {
    /**
     * ID
     */
    private Integer id;

    /**
     * 作者昵称
     */
    private String name;
    /**
     * 账号(手机号)
     */
    private String account;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 作者头像地址
     */
    private String avatar;
    /**
     * 作者简介
     */
    private String introduction;
    /**
     * 等级：1-执笔者，2-织梦师，3-造界者，4-渡舟人，5-燃灯者
     */
    private Integer rank;
    /**
     * 作品数量（冗余字段，用于排行榜统计）
     */
    private Integer novelCount;
    /**
     * 是否删除：false-否，true-是
     */
    private Boolean isDel;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

}