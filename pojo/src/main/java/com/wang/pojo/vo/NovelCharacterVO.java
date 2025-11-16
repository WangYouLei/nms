package com.wang.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * 小说角色VO类
 * 用于后端返回角色数据
 */
@Data
public class NovelCharacterVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 备注
     */
    private String remark;

    /**
     * 角色类别：0-主角,1-主角伙伴,2-主角伴侣,3-主角家人,4-角色师傅,5-反派,6-墙头草,7-亦正亦邪,8-其他
     */
    private String category;//这里就将数据转为字符串，省的前端再转换

    /**
     * 角色图片地址
     */
    private String url;

    /**
     * 角色等级
     */
    private String level;

    /**
     * 角色能力(角色能力 && 能力等级)
     */
    private List<CharacterAbilityVO> list;
}