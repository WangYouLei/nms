package com.wang.pojo.dto;

import lombok.Data;
import java.io.Serializable;

/**
* 管理人信息DTO
* @TableName manager
*/
@Data
public class ManagerDTO implements Serializable {

    /**
     * 管理员id
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
    * 密码
    */
    private String password;
    /**
    * 头像
    */
    private String avatar;

}
