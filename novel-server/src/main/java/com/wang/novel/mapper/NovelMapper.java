package com.wang.novel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.Novel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小说Mapper接口
 */
@Mapper
public interface NovelMapper extends BaseMapper<Novel> {
}