package com.wang.manage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.CharacterAbility;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CharacterAbilityMapper extends BaseMapper<CharacterAbility> {
    /**
     * 批量插入角色技能关联
     * @param characterAbilityList 角色技能关联列表
     */
    void insertBatch(List<CharacterAbility> characterAbilityList);

    /**
     * 批量更新角色技能关联
     * @param characterAbilities 角色技能关联列表
     */
    void updateBatch(List<CharacterAbility> characterAbilities);
}
