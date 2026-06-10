package com.wang.pojo.entity;

import java.io.Serializable;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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
    @ApiModelProperty("作者ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
    * 作者昵称
    */
    @ApiModelProperty("作者昵称")
    private String name;
    /**
    * 账号(手机号)
    */
    @ApiModelProperty("账号(手机号)")
    private String account;
    /**
    * 密码
    */
    @ApiModelProperty("密码")
    private String password;
    /**
    * 邮箱
    */
    @ApiModelProperty("邮箱")
    private String email;
/**
     * 作者头像地址
     */
    @ApiModelProperty("作者头像地址")
    private String avatar;
    /**
     * 作者简介
     */
    @ApiModelProperty("作者简介")
    private String introduction;
/**
     * 等级：1-执笔者，2-织梦师，3-造界者，4-渡舟人，5-燃灯者
     */
    @ApiModelProperty("等级：1-执笔者，2-织梦师，3-造界者，4-渡舟人，5-燃灯者")
    private Integer rank;
    /**
     * 作品数量（冗余字段，用于排行榜统计）
     */
    @ApiModelProperty("作品数量（冗余字段，用于排行榜统计）")
    private Integer novelCount;
    /**
     * 是否删除：false-否，true-是
     */
    @ApiModelProperty("是否删除：false-否，true-是")
    @TableLogic
    private Boolean isDel;
    /**
    * 创建时间
    */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    /**
    * 修改时间
    */
    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

}
