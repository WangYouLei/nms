package com.wang.manage.service;

import com.wang.common.result.Result;
import com.wang.pojo.dto.AbilityLevelDTO;
import com.wang.pojo.dto.CharacterAbilityDTO;
import com.wang.pojo.vo.CharacterALVO;

import java.util.List;

public interface CharacterAbilityService {
    /**
     * 批量插入角色技能关联
     * @param characterAbilityList 角色技能关联列表
     */
    void insertAbilityBatch(List<CharacterAbilityDTO> characterAbilityList);

    /**
     * 添加角色能力等级
     * @return
     */
    Result addLevel(List<AbilityLevelDTO> list);

    /**
     * 添加角色能力
     * @return
     */
    Result addAbility(List<CharacterAbilityDTO>  list,Integer characterId);

    /**
     * 获取角色能力和对应的等级列表
     * @return
     */
    List<CharacterALVO> getAbilityList(Integer id);
}
