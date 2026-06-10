package com.wang.novel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.NovelCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小说分类Mapper接口
 */
@Mapper
public interface NovelCategoryMapper extends BaseMapper<NovelCategory> {

    /**
     * 动态更新小说分类，只更新非 null 的字段
     * @param novelCategory 分类信息
     * @return 影响行数
     */
    int updateSelective(NovelCategory novelCategory);
}