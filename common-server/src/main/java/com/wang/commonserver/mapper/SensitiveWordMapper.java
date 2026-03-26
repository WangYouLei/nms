package com.wang.commonserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 敏感词Mapper接口
 */
@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {

    /**
     * 动态更新敏感词，只更新非 null 的字段
     * @param sensitiveWord 敏感词信息
     * @return 影响行数
     */
    int update(SensitiveWord sensitiveWord);

    /**
     * 查询所有启用的敏感词
     * @return 敏感词列表
     */
    @Select("SELECT * FROM sensitive_word WHERE status = 1")
    List<SensitiveWord> selectAllEnabled();

    /**
     * 根据敏感词查询
     * @param word 敏感词
     * @return 敏感词实体
     */
    @Select("SELECT * FROM sensitive_word WHERE word = #{word}")
    SensitiveWord selectByWord(@Param("word") String word);

    /**
     * 根据类别查询启用的敏感词
     * @param category 类别
     * @return 敏感词列表
     */
    @Select("SELECT * FROM sensitive_word WHERE category = #{category} AND status = 1")
    List<SensitiveWord> selectByCategory(@Param("category") Integer category);

    /**
     * 根据等级查询启用的敏感词
     * @param level 等级
     * @return 敏感词列表
     */
    @Select("SELECT * FROM sensitive_word WHERE level = #{level} AND status = 1")
    List<SensitiveWord> selectByLevel(@Param("level") Integer level);
}