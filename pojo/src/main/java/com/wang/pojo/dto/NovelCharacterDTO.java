package com.wang.pojo.dto;

import com.wang.pojo.entity.CharacterAbility;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 小说角色DTO类
 * 用于前端传递角色数据
 */
@Data
public class NovelCharacterDTO {

    /**
     * 角色ID
     */
    private Integer id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 小说ID（关联小说表）
     */
    private Integer novelId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 角色类别：0-主角,1-主角伙伴,2-主角伴侣,3-主角家人,4-角色师傅,5-反派,6-墙头草,7-亦正亦邪,8-其他
     */
    private Integer category;

    /**
     * 角色图片地址
     */
    private String url;

    /**
     * 角色等级（关联等级表）
     */
    private String stage;

    /**
     * 角色技能列表
     */
    private List<CharacterAbilityDTO> characterAbilityList;
}