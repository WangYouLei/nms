package com.wang.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.Author;
import org.apache.ibatis.annotations.Mapper;

/**
 * 作者Mapper
 */
@Mapper
public interface AuthorMapper extends BaseMapper<Author> {
}