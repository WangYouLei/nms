package com.wang.pojo.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

/**
* 小说三级分类表
* @TableName novel_level3_category
*/
public class NovelLevel3Category implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
    * 分类名称
    */
    @NotBlank(message="[分类名称]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @ApiModelProperty("分类名称")
    @Length(max= 50,message="编码长度不能超过50")
    private String name;
    /**
    * 热门标签：0-否 1-是
    */
    @NotNull(message="[热门标签：0-否 1-是]不能为空")
    @ApiModelProperty("热门标签：0-否 1-是")
    private Integer isHot;
    /**
    * 二级分类ID
    */
    @NotNull(message="[二级分类ID]不能为空")
    @ApiModelProperty("二级分类ID")
    private Integer level2Id;



}
