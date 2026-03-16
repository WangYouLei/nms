package com.wang.novel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.NovelCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小说分类Mapper接口
 */
@Mapper
public interface NovelCategoryMapper extends BaseMapper<NovelCategory> {
}