package com.wang.pojo.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
* 小说一级分类表
* @TableName novel_level1_category
*/
@Data
public class NovelLevel1Category implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    private Integer id;
    /**
    * 分类名称
    */
    @NotBlank(message="[分类名称]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @ApiModelProperty("分类名称")
    @Length(max= 50,message="编码长度不能超过50")
    private String name;

}
