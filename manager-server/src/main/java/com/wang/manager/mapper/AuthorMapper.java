package com.wang.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.Author;
import org.apache.ibatis.annotations.Mapper;

/**
 * 作者Mapper
 *
 * 【跨域只读查询 - CQRS 读模型】
 * 本 Mapper 访问 author 表（归属 author-server），仅用于管理后台的查询操作。
 * 约束：只允许 SELECT，禁止 INSERT/UPDATE/DELETE，修改操作必须通过 Feign 调用 author-server。
 */
@Mapper
public interface AuthorMapper extends BaseMapper<Author> {
}