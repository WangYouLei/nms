package com.wang.comment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户头像Mapper接口
 * 用于查询不同类型用户的头像
 */
@Mapper
public interface UserAvatarMapper {

    /**
     * 根据作者ID查询头像
     * @param userId 作者ID
     * @return 头像URL
     */
    @Select("SELECT avatar FROM author WHERE id = #{userId}")
    String selectAuthorAvatarById(@Param("userId") Long userId);

    /**
     * 根据访客ID查询头像
     * @param userId 访客ID
     * @return 头像URL
     */
    @Select("SELECT avatar FROM visitor WHERE id = #{userId}")
    String selectVisitorAvatarById(@Param("userId") Long userId);

    /**
     * 根据管理员ID查询头像
     * @param userId 管理员ID
     * @return 头像URL
     */
    @Select("SELECT avatar FROM manager WHERE id = #{userId}")
    String selectManagerAvatarById(@Param("userId") Long userId);
}