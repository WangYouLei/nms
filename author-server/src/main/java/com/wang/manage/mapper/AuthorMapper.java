package com.wang.manage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.Author;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface AuthorMapper extends BaseMapper<Author> {
}