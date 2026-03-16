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
     * 作者头像地址
     */
    private String avatar;
    /**
     * 等级：1-执笔者，2-织梦师，3-造界者，4-渡舟人，5-燃灯者
     */
    private Integer rank;
    /**
     * 是否删除：0-否，1-是
     */
    private Integer isDel;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

}