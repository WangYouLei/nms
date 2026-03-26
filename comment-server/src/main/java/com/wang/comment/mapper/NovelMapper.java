package com.wang.comment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 小说Mapper接口（仅用于查询小说作者ID）
 */
@Mapper
public interface NovelMapper {

    /**
     * 根据小说ID查询作者ID
     * @param novelId 小说ID
     * @return 作者ID
     */
    @Select("SELECT author_id FROM novel WHERE id = #{novelId}")
    Integer selectAuthorIdById(@Param("novelId") Long novelId);
}