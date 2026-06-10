package com.wang.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.Visitor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 访客Mapper
 *
 * 【跨域只读查询 - CQRS 读模型】
 * 本 Mapper 访问 visitor 表（归属 visitor-server），仅用于管理后台的查询操作。
 * 约束：只允许 SELECT，禁止 INSERT/UPDATE/DELETE，修改操作必须通过 Feign 调用 visitor-server。
 */
@Mapper
public interface VisitorMapper extends BaseMapper<Visitor> {
}