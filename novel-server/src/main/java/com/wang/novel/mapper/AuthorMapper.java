package com.wang.novel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.Author;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 作者Mapper接口（用于novel-server模块查询作者信息）
 */
@Mapper
public interface AuthorMapper extends BaseMapper<Author> {

    /**
     * 根据作者ID查询头像
     */
    @Select("SELECT avatar FROM author WHERE id = #{authorId}")
    String selectAvatarById(Integer authorId);

    /**
     * 根据作者ID查询作品数量
     */
    @Select("SELECT COUNT(*) FROM novel WHERE author_id = #{authorId} AND is_del = 0")
    Integer countNovelsByAuthorId(Integer authorId);

    /**
     * 根据作者ID查询作者基本信息
     */
    @Select("SELECT id, name, avatar, introduction, rank, novel_count FROM author WHERE id = #{authorId} AND is_del = 0")
    Author selectAuthorBasicInfo(Integer authorId);
}