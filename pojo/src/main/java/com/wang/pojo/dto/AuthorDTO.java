package com.wang.pojo.dto;

import lombok.Data;
import java.io.Serializable;

/**
* 作者信息DTO
* @TableName author
*/
@Data
public class AuthorDTO implements Serializable {

    /**
     * 作者id
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
    * 密码
    */
    private String password;
    /**
    * 头像
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

}