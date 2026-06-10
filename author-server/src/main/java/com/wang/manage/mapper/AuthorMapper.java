package com.wang.manage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.Author;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface AuthorMapper extends BaseMapper<Author> {

    /**
     * 动态更新作者信息，只更新非 null 的字段
     * @param author 作者信息
     * @return 影响行数
     */
    int updateSelective(Author author);
}