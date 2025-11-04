package com.wang.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
* 管理人信息DTO
* @TableName manager
*/
@Data
public class ManagerDTO implements Serializable {
    /**
    * 管理人ID
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
    * 头像URL
    */
    private String avatar;


}
