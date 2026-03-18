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
    * 邮箱
    */
    private String email;
    /**
    * 头像
    */
    private String avatar;
    /**
    * 等级：1-执笔者，2-织梦师，3-造界者，4-渡舟人，5-燃灯者
    */
    //设置默认值：1
    private Integer rank = 1;
    /**
    * 是否删除：false-否，true-是
    */
    private Boolean isDel;

}