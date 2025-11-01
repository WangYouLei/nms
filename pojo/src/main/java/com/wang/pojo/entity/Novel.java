package com.wang.pojo.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
* 小说信息表
* @TableName novel
*/
@Data
public class Novel implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    private Integer id;
    /**
    * 小说名称
    */
    @NotBlank(message="[小说名称]不能为空")
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("小说名称")
    @Length(max= 255,message="编码长度不能超过255")
    private String name;
    /**
    * 小说副名称
    */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("小说副名称")
    @Length(max= 255,message="编码长度不能超过255")
    private String subName;
    /**
    * 小说标签
    */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("小说标签")
    @Length(max= 255,message="编码长度不能超过255")
    private String tags;
    /**
    * 小说简介
    */
    @Size(max= -1,message="编码长度不能超过-1")
    @ApiModelProperty("小说简介")
    @Length(max= -1,message="编码长度不能超过-1")
    private String introduction;
    /**
    * 作者ID（关联管理人表）
    */
    @NotNull(message="[作者ID（关联管理人表）]不能为空")
    @ApiModelProperty("作者ID（关联管理人表）")
    private Integer authorId;

}
