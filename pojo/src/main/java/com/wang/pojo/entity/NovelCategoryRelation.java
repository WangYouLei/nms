package com.wang.pojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

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
    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
    * 小说ID（关联小说表）
    */
    @ApiModelProperty("小说ID")
    private Long novelId;
    /**
    * 分类ID（关联分类表）
    */
    @ApiModelProperty("分类ID")
    private Long categoryId;

    private LocalDateTime createTime;

}
