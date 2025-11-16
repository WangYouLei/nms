package com.wang.manage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.pojo.entity.NovelCharacter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 小说角色Mapper接口
 */
@Mapper
public interface NovelCharacterMapper extends BaseMapper<NovelCharacter> {
    
   /* *//**
     * 分页查询本小说角色（按照角色类别进行排序，数字越小越靠前）
     * @param page 分页参数
     * @param novelId 小说ID
     * @return 分页结果
     *//*
    IPage<NovelCharacter> selectPageByNovelId(Page<NovelCharacter> page, @Param("novelId") Integer novelId);
    
    *//**
     * 根据角色名称进行模糊查询
     * @param page 分页参数
     * @param name 角色名称
     * @param novelId 小说ID
     * @return 分页结果
     *//*
    IPage<NovelCharacter> selectPageByName(Page<NovelCharacter> page, @Param("name") String name, @Param("novelId") Integer novelId);*/

    /**
     * 添加角色信息并返回角色ID
     * @param novelCharacter 角色信息
     * @return 角色ID
     */
    void insertAndGetId(NovelCharacter novelCharacter);
}