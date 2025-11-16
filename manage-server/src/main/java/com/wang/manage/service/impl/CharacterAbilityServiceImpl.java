package com.wang.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wang.common.result.Result;
import com.wang.manage.mapper.AbilityLevelMapper;
import com.wang.manage.mapper.CharacterAbilityMapper;
import com.wang.manage.service.CharacterAbilityService;
import com.wang.pojo.dto.AbilityLevelDTO;
import com.wang.pojo.dto.CharacterAbilityDTO;
import com.wang.pojo.entity.AbilityLevel;
import com.wang.pojo.entity.CharacterAbility;
import com.wang.pojo.vo.CharacterAbilityVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CharacterAbilityServiceImpl implements CharacterAbilityService {
    @Autowired
    private CharacterAbilityMapper characterAbilityMapper;

    @Autowired
    private AbilityLevelMapper abilityLevelMapper;

    @Override
    public void insertAbilityBatch(List<CharacterAbilityDTO> characterAbilityList) {
        List<CharacterAbility> characterAbilities = convertToEntity(characterAbilityList, CharacterAbility.class);
        characterAbilityMapper.insertBatch(characterAbilities);
    }

    @Override
    public Result addLevel(List<AbilityLevelDTO> list) {
        List<AbilityLevel> abilityLevels = convertToEntity(list, AbilityLevel.class);
        abilityLevelMapper.insertBatch(abilityLevels);
        return Result.success();
    }

    @Override
    public Result addAbility(List<CharacterAbilityDTO> list,Integer characterId) {
        List<CharacterAbility> characterAbilities = convertToEntity(list, CharacterAbility.class);

        //批量设置角色id
        characterAbilities = characterAbilities.stream()
                .peek(ca -> ca.setCharacterId(characterId))
                .collect(Collectors.toList());
        characterAbilityMapper.insertBatch(characterAbilities);
        return Result.success();
    }

    @Override
    public List<CharacterAbilityVO> getAbilityList(Integer id) {
        List<CharacterAbility> characterAbilities = characterAbilityMapper.selectList(new QueryWrapper<>(CharacterAbility.class).eq("character_id", id));
        List<CharacterAbilityVO> characterAbilityVOS = characterAbilities.stream().map(characterAbility -> {
            CharacterAbilityVO characterAbilityVO = new CharacterAbilityVO();
            org.springframework.beans.BeanUtils.copyProperties(characterAbility, characterAbilityVO);
            return characterAbilityVO;
        }).collect(Collectors.toList());
        return characterAbilityVOS;
    }

    @Override
    public void updateAbility(List<CharacterAbilityDTO> characterAbilityList) {
        List<CharacterAbility> characterAbilities = convertToEntity(characterAbilityList, CharacterAbility.class);
        characterAbilityMapper.updateById(characterAbilities);
    }


    /**
     * dot类批量转换为实体类
     * @param list
     * @param clazz
     * @return
     * @param <D> dot类
     * @param <T> 实体类
     */
   private <D, T> List<T> convertToEntity(List<D> list, Class<T> clazz) {
    return list.stream().map(d -> {
        try {
            T entity = clazz.getDeclaredConstructor().newInstance();
            org.springframework.beans.BeanUtils.copyProperties(d, entity);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("转换失败", e);
        }
    }).collect(java.util.stream.Collectors.toList());
}

}
