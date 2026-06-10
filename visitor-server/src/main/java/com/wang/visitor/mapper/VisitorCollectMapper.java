package com.wang.visitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.VisitorCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 小说收藏Mapper接口
 */
@Mapper
public interface VisitorCollectMapper extends BaseMapper<VisitorCollect> {

    /**
     * 统计用户收藏数量
     */
    @Select("SELECT COUNT(*) FROM visitor_collect WHERE visitor_id = #{visitorId}")
    Long countByVisitorId(@Param("visitorId") Long visitorId);

    /**
     * 统计小说被收藏数量
     */
    @Select("SELECT COUNT(*) FROM visitor_collect WHERE novel_id = #{novelId}")
    Long countByNovelId(@Param("novelId") Long novelId);
}