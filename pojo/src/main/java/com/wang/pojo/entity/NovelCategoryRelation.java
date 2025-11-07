package com.wang.pojo.entity;

import javax.validation.constraints.NotNull;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
* 小说与三级分类关联表
* @TableName novel_category_relation
*/
@Data
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
    @NotNull(message="[小说ID（关联小说表）]不能为空")
    @ApiModelProperty("小说ID（关联小说表）")
    private Integer novelId;
    /**
    * 三级分类ID（关联三级分类表）
    */
    @NotNull(message="[三级分类ID（关联三级分类表）]不能为空")
    @ApiModelProperty("三级分类ID（关联三级分类表）")
    private Integer level3Id;

}
