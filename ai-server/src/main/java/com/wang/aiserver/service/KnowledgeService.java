package com.wang.aiserver.service;

import com.wang.aiserver.extractor.KnowledgeExtractor;
import com.wang.pojo.entity.KnowledgeItem;

import java.util.List;

/**
 * 知识管理服务接口
 */
public interface KnowledgeService {

    /**
     * 从章节文本提取知识并存储
     * @return [newItems, updatedItems]
     */
    KnowledgeExtractResult extractAndStore(Long novelId, Long chapterId, int chapterOrder,
                                            String chapterText, String novelInfo);

    /**
     * 获取小说的写作风格总结
     */
    StyleSummaryResult getStyleSummary(Long novelId);

    /**
     * 更新写作风格总结（AI 提炼）
     */
    StyleSummaryResult updateStyleSummary(Long novelId, String chapterSamples);

    /**
     * 手动保存写作风格总结
     */
    StyleSummaryResult saveStyleSummary(Long novelId, String styleText, int lastChapter);

    /**
     * 获取小说的所有知识项
     */
    List<KnowledgeItem> getAllForNovel(Long novelId, String itemType, int minStatus);

    /**
     * 创建或更新知识项
     */
    KnowledgeItem saveItem(KnowledgeItem item);

    /**
     * 更新知识项状态（审批）
     */
    KnowledgeItem updateItemStatus(Long id, int status);

    /**
     * 删除知识项
     */
    void deleteItem(Long id);
}
