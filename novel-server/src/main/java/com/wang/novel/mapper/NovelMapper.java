package com.wang.novel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.Novel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 小说Mapper接口
 * 公共接口，三个端口共用
 */
@Mapper
public interface NovelMapper extends BaseMapper<Novel> {

    /**
     * 分页查询热门小说（支持分类筛选）
     * @param categoryId 分类ID（可选）
     * @param offset 偏移量
     * @param pageSize 每页数量
     * @return 热门小说列表
     */
    List<Novel> selectHotNovelsByPage(@Param("categoryId") Integer categoryId,
                                       @Param("offset") Integer offset,
                                       @Param("pageSize") Integer pageSize);

    /**
     * 统计热门小说总数（支持分类筛选）
     * @param categoryId 分类ID（可选）
     * @return 总数
     */
    Integer countHotNovels(@Param("categoryId") Integer categoryId);


    /**
     * 根据分类ID分页查询小说
     * @param categoryId 分类ID
     * @param offset 偏移量
     * @param pageSize 每页数量
     * @return 小说列表
     */
    List<Novel> selectNovelsByCategoryId(@Param("categoryId") Integer categoryId,
                                          @Param("offset") Integer offset,
                                          @Param("pageSize") Integer pageSize);

    /**
     * 统计分类下小说总数
     * @param categoryId 分类ID
     * @return 总数
     */
    Integer countNovelsByCategoryId(@Param("categoryId") Integer categoryId);

}