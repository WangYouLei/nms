package com.wang.visitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.Novel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 小说Mapper接口（用于visitor-server模块查询小说信息）
 */
@Mapper
public interface NovelMapper extends BaseMapper<Novel> {

    /**
     * 根据小说ID查询小说基本信息
     */
    @Select("SELECT * FROM novel WHERE id = #{novelId} AND is_del = 0")
    Novel selectNovelById(Integer novelId);
}