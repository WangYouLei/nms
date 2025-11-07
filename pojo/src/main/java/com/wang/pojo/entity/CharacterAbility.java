package com.wang.pojo.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
* 角色能力表
* @TableName character_ability
*/
@Data
public class CharacterAbility implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
    * 角色ID（关联角色表）
    */
    @NotNull(message="[角色ID（关联角色表）]不能为空")
    @ApiModelProperty("角色ID（关联角色表）")
    private Integer characterId;
    /**
    * 能力名称
    */
    @NotBlank(message="[能力名称]不能为空")
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("能力名称")
    @Length(max= 20,message="编码长度不能超过20")
    private String ability;
    /**
    * 能力级别ID（关联能力等级表）
    */
    @ApiModelProperty("能力级别ID（关联能力等级表）")
    private Integer abilityLevelId;
    /**
    * 备注
    */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("备注")
    @Length(max= 255,message="编码长度不能超过255")
    private String remark;

}
