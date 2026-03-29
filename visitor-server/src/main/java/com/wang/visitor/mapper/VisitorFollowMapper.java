package com.wang.visitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.VisitorFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 访客关注作者Mapper接口
 */
@Mapper
public interface VisitorFollowMapper extends BaseMapper<VisitorFollow> {

    /**
     * 查询访客关注某作者的数量
     * @param visitorId 访客ID
     * @return 关注数量
     */
    @Select("SELECT COUNT(*) FROM visitor_follow WHERE visitor_id = #{visitorId}")
    int countByVisitorId(@Param("visitorId") Integer visitorId);

    /**
     * 查询某作者的粉丝数量
     * @param authorId 作者ID
     * @return 粉丝数量
     */
    @Select("SELECT COUNT(*) FROM visitor_follow WHERE author_id = #{authorId}")
    int countByAuthorId(@Param("authorId") Integer authorId);

    /**
     * 检查是否已关注
     * @param visitorId 访客ID
     * @param authorId 作者ID
     * @return 是否已关注
     */
    @Select("SELECT COUNT(*) > 0 FROM visitor_follow WHERE visitor_id = #{visitorId} AND author_id = #{authorId}")
    boolean existsByVisitorIdAndAuthorId(@Param("visitorId") Integer visitorId, @Param("authorId") Integer authorId);
}