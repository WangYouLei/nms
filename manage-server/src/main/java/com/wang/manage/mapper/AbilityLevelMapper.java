package com.wang.manage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.AbilityLevel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AbilityLevelMapper extends BaseMapper<AbilityLevel> {
    /**
     * 批量插入
     * @param abilityLevels
     */
    void insertBatch(List<AbilityLevel> abilityLevels);
}
