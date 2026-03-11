package com.wang.visitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.Visitor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 访客Mapper接口
 */
@Mapper
public interface VisitorMapper extends BaseMapper<Visitor> {
}