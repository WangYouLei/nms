package com.wang.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理人信息DTO
 *
 * @TableName manager
 */
@Data
public class ManagerVO implements Serializable {
    /**
     * ID
     */
    private Integer id;

    /**
     * 管理人名称
     */
    private String name;
    /**
     * 账号(手机号)
     */
    private String account;
    /**
     * 头像URL
     */
    private String avatar;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建本账号的管理者
     */
    private Integer createManager;

    /**
     * 修改本账号的管理者
     */
    private Integer updateManager;

}
