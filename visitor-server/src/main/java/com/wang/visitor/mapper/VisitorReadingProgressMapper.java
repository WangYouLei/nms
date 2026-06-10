package com.wang.visitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.VisitorReadingProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VisitorReadingProgressMapper extends BaseMapper<VisitorReadingProgress> {

    @Select("SELECT COUNT(*) FROM visitor_reading_progress WHERE novel_id = #{novelId}")
    Long countByNovelId(@Param("novelId") Long novelId);
}
