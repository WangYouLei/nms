package com.wang.novel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.NovelCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 访客端分类Mapper接口
 */
@Mapper
public interface VisitorCategoryMapper extends BaseMapper<NovelCategory> {
}