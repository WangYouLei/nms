package com.wang.pojo.dto;

import lombok.Data;

@Data
public class CharacterAbilityDTO {
    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 能力名称
     */
    private String ability;

    /**
     * 角色ID（关联角色表）
     */
    private Integer characterId;

    /**
     * 能力级别（关联能力等级表）
     */
    private String abilityLevel;

    /**
     * 备注
     */
    private String remark;

}
