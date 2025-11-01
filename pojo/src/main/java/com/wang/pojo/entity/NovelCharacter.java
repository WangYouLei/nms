package com.wang.pojo.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
* 小说角色表
* @TableName novel_character
*/
@Data
public class NovelCharacter implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    private Integer id;
    /**
    * 角色名称
    */
    @NotBlank(message="[角色名称]不能为空")
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("角色名称")
    @Length(max= 20,message="编码长度不能超过20")
    private String name;
    /**
    * 角色类别：0-主角,1-主角伙伴,2-主角伴侣,3-主角家人,4-角色师傅,5-反派,6-墙头草,7-亦正亦邪,8-其他
    */
    @NotNull(message="[角色类别：0-主角,1-主角伙伴,2-主角伴侣,3-主角家人,4-角色师傅,5-反派,6-墙头草,7-亦正亦邪,8-其他]不能为空")
    @ApiModelProperty("角色类别：0-主角,1-主角伙伴,2-主角伴侣,3-主角家人,4-角色师傅,5-反派,6-墙头草,7-亦正亦邪,8-其他")
    private Integer category;
    /**
    * 角色等级（关联等级表）
    */
    @ApiModelProperty("角色等级（关联等级表）")
    private Integer levelId;
    /**
    * 备注
    */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("备注")
    @Length(max= 255,message="编码长度不能超过255")
    private String remark;
    /**
    * 小说ID（关联小说表）
    */
    @NotNull(message="[小说ID（关联小说表）]不能为空")
    @ApiModelProperty("小说ID（关联小说表）")
    private Integer novelId;

}
