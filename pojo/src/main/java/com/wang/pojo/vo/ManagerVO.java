package com.wang.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员VO
 * @TableName manager
 */
@Data
public class ManagerVO implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 昵称
     */
    private String name;

    /**
     * 账号
     */
    private String account;

    /**
     * 创建者ID
     */
    private Long createId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}