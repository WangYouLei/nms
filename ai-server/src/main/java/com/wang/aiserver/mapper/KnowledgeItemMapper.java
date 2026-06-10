package com.wang.aiserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.KnowledgeItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgeItemMapper extends BaseMapper<KnowledgeItem> {

    @Select("SELECT id, item_type, name, summary FROM knowledge_items WHERE novel_id = #{novelId} AND status >= 0 ORDER BY id LIMIT #{limit}")
    List<KnowledgeItem> findExistingSummary(@Param("novelId") Long novelId, @Param("limit") int limit);

    @Select("SELECT * FROM knowledge_items WHERE novel_id = #{novelId} AND item_type = #{itemType} AND name = #{name} LIMIT 1")
    KnowledgeItem findByNovelTypeName(@Param("novelId") Long novelId,
                                       @Param("itemType") String itemType,
                                       @Param("name") String name);

    @Select("SELECT * FROM knowledge_items WHERE novel_id = #{novelId} AND item_type = #{itemType}")
    List<KnowledgeItem> findByNovelAndType(@Param("novelId") Long novelId,
                                           @Param("itemType") String itemType);

    @Select("SELECT * FROM knowledge_items WHERE novel_id = #{novelId} AND status >= #{minStatus} ORDER BY updated_at DESC")
    List<KnowledgeItem> findByNovelAndMinStatus(@Param("novelId") Long novelId,
                                                 @Param("minStatus") int minStatus);

    List<KnowledgeItem> findByIdList(@Param("ids") List<Long> ids);
}
