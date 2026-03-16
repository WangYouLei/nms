package com.wang.pojo.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
* 作者信息表
* @TableName author
*/
@Data
@TableName("author")
public class Author implements Serializable {

    /**
    * 作者ID
    */
    @NotNull(message="[作者ID]不能为空")
    @ApiModelProperty("作者ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
    * 作者昵称
    */
    @NotBlank(message="[作者昵称]不能为空")
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("作者昵称")
    @Length(max= 20,message="编码长度不能超过20")
    private String name;
    /**
    * 账号(手机号)
    */
    @NotBlank(message="[账号(手机号)]不能为空")
    @Size(max= 11,message="编码长度不能超过11")
    @ApiModelProperty("账号(手机号)")
    @Length(max= 11,message="编码长度不能超过11")
    private String account;
    /**
    * 密码
    */
    @NotBlank(message="[密码]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @ApiModelProperty("密码")
    @Length(max= 100,message="编码长度不能超过100")
    private String password;
    /**
    * 作者头像地址
    */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("作者头像地址")
    @Length(max= 255,message="编码长度不能超过255")
    private String avatar;
    /**
    * 等级：1-执笔者，2-织梦师，3-造界者，4-渡舟人，5-燃灯者
    */
    @NotNull(message="[等级]不能为空")
    @ApiModelProperty("等级：1-执笔者，2-织梦师，3-造界者，4-渡舟人，5-燃灯者")
    private Integer rank;
    /**
    * 是否删除：0-否，1-是
    */
    @ApiModelProperty("是否删除：0-否，1-是")
    @TableLogic
    private Integer isDel;
    /**
    * 创建时间
    */
    @NotNull(message="[创建时间]不能为空")
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    /**
    * 修改时间
    */
    @NotNull(message="[修改时间]不能为空")
    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

}