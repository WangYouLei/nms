package com.wang.novel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.NovelCategoryRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小说分类关联Mapper接口
 */
@Mapper
public interface NovelCategoryRelationMapper extends BaseMapper<NovelCategoryRelation> {
}