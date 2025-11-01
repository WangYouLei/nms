package com.wang.pojo.entity;

import javax.validation.constraints.NotNull;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
* 角色与势力关联表
* @TableName character_faction
*/
@Data
public class CharacterFaction implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    private Integer id;
    /**
    * 角色ID（关联角色表）
    */
    @NotNull(message="[角色ID（关联角色表）]不能为空")
    @ApiModelProperty("角色ID（关联角色表）")
    private Integer characterId;
    /**
    * 势力ID（关联势力表）
    */
    @NotNull(message="[势力ID（关联势力表）]不能为空")
    @ApiModelProperty("势力ID（关联势力表）")
    private Integer factionId;


}
