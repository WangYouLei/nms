package com.wang.visitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.Visitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface VisitorMapper extends BaseMapper<Visitor> {

    /**
     * 动态更新访客信息，只更新非 null 的字段
     * @param visitor 访客信息
     * @return 影响行数
     */
    int update(Visitor visitor);
}