package com.wang.pojo.entity;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
* 小说分类关联表
* @TableName novel_category_relation
*/
@Data
@TableName("novel_category_relation")
public class NovelCategoryRelation implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
    * 小说ID（关联小说表）
    */
    @NotNull(message="[小说ID]不能为空")
    @ApiModelProperty("小说ID")
    private Integer novelId;
    /**
    * 分类ID（关联分类表）
    */
    @NotNull(message="[分类ID]不能为空")
    @ApiModelProperty("分类ID")
    private Integer categoryId;

}