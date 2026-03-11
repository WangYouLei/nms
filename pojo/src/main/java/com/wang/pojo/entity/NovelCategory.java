package com.wang.pojo.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
* 小说分类表
* @TableName novel_category
*/
@Data
@TableName("novel_category")
public class NovelCategory implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
    * 分类类型
    */
    @NotBlank(message="[分类类型]不能为空")
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("分类类型")
    @Length(max= 20,message="编码长度不能超过20")
    private String type;

    /**
    * 频道：1-男频,2-女频
    */
    @NotNull(message="[频道]不能为空")
    @ApiModelProperty("频道：1-男频,2-女频")
    private Integer category;

    /**
    * 是否是热门标签：0-否,1-是
    */
    @NotNull(message="[是否热门]不能为空")
    @ApiModelProperty("是否是热门标签：0-否,1-是")
    private Integer isHot;

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