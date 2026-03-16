package com.wang.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员DTO
 * @TableName manager
 */
@Data
public class ManagerDTO implements Serializable {

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
     * 密码
     */
    private String password;

    /**
     * 创建者ID
     */
    private Long createId;

}