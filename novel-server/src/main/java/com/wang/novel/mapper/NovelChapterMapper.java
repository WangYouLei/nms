package com.wang.novel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.NovelChapter;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小说章节Mapper接口
 */
@Mapper
public interface NovelChapterMapper extends BaseMapper<NovelChapter> {

    /**
     * 动态更新小说章节，只更新非 null 的字段
     * @param novelChapter 章节信息
     * @return 影响行数
     */
    int update(NovelChapter novelChapter);
}