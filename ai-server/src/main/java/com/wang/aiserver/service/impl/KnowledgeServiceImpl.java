package com.wang.aiserver.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wang.aiserver.extractor.KnowledgeExtractor;
import com.wang.aiserver.mapper.KnowledgeItemMapper;
import com.wang.aiserver.service.KnowledgeExtractResult;
import com.wang.aiserver.service.KnowledgeService;
import com.wang.aiserver.service.StyleSummaryResult;
import com.wang.pojo.entity.KnowledgeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识管理服务实现 - 对应 Python services/knowledge_service.py
 * Wiki方式：知识项仅存MySQL，不再同步向量索引
 */
@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeServiceImpl.class);

    private static final String TYPE_STYLE_SUMMARY = "style_summary";
    private static final int DEFAULT_TOP = 50;

    private final KnowledgeItemMapper knowledgeItemMapper;
    private final KnowledgeExtractor knowledgeExtractor;
    private final ObjectMapper objectMapper;

    public KnowledgeServiceImpl(KnowledgeItemMapper knowledgeItemMapper,
                                 KnowledgeExtractor knowledgeExtractor,
                                 ObjectMapper objectMapper) {
        this.knowledgeItemMapper = knowledgeItemMapper;
        this.knowledgeExtractor = knowledgeExtractor;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public KnowledgeExtractResult extractAndStore(Long novelId, Long chapterId, int chapterOrder,
                                                   String chapterText, String novelInfo) {
        String existingSummary = getExistingSummary(novelId);

        List<KnowledgeExtractor.ExtractedEntity> entities = knowledgeExtractor.extractFromChapter(
                chapterText, novelInfo, existingSummary, chapterOrder);

        List<Object> newItems = new ArrayList<>();
        List<Object> updatedItems = new ArrayList<>();

        for (KnowledgeExtractor.ExtractedEntity entity : entities) {
            UpsertResult result = upsertEntity(novelId, chapterId, entity);
            if (result.isNew) {
                newItems.add(entitySummary(result.item));
            } else {
                updatedItems.add(entitySummary(result.item));
            }
        }

        log.info("知识提取完成: novelId={}, 总数={}, 新增={}, 更新={}",
                novelId, entities.size(), newItems.size(), updatedItems.size());

        return new KnowledgeExtractResult(entities.size(), newItems, updatedItems);
    }

    @Override
    public StyleSummaryResult getStyleSummary(Long novelId) {
        List<KnowledgeItem> items = knowledgeItemMapper.findByNovelAndType(novelId, TYPE_STYLE_SUMMARY);
        if (items.isEmpty()) {
            return new StyleSummaryResult("", 0, 0, false);
        }

        KnowledgeItem item = items.get(0);
        // 优先从 content JSON 读取完整 style_text，fallback 到 summary
        String styleText = extractStyleTextFromContent(item.getContent());
        if (styleText == null || styleText.isBlank()) {
            styleText = item.getSummary() != null ? item.getSummary() : "";
        }
        return new StyleSummaryResult(styleText, getLastChapter(item), item.getVersion(), true);
    }

    @Override
    @Transactional
    public StyleSummaryResult updateStyleSummary(Long novelId, String chapterSamples) {
        StyleSummaryResult existing = getStyleSummary(novelId);
        String existingStyle = existing.isExists() ? existing.getStyleText() : "";

        String newStyle = knowledgeExtractor.extractStyleSummary(chapterSamples, existingStyle);
        if (newStyle == null || newStyle.isBlank()) {
            return existing;
        }

        int lastChapter = existing.isExists() ? existing.getLastSummarizedChapter() : 0;
        return saveStyleSummary(novelId, newStyle, lastChapter);
    }

    @Override
    @Transactional
    public StyleSummaryResult saveStyleSummary(Long novelId, String styleText, int lastChapter) {
        List<KnowledgeItem> items = knowledgeItemMapper.findByNovelAndType(novelId, TYPE_STYLE_SUMMARY);
        KnowledgeItem item;
        int version;

        if (items.isEmpty()) {
            item = new KnowledgeItem();
            item.setNovelId(novelId);
            item.setItemType(TYPE_STYLE_SUMMARY);
            item.setName("写作风格总结");
            item.setStatus(0);
            item.setConfidence(0.8);
            version = 1;
        } else {
            item = items.get(0);
            version = item.getVersion() + 1;
            item.setStatus(2); // 已修改
        }

        item.setSummary(styleText.length() > 200 ? styleText.substring(0, 200) : styleText);
        item.setVersion(version);
        item.setSourceChapterOrder(lastChapter);

        // 保存 styleText 和 lastChapter 到 content JSON 中
        try {
            Map<String, Object> content = new HashMap<>();
            content.put("style_text", styleText);
            content.put("last_summarized_chapter", lastChapter);
            item.setContent(objectMapper.writeValueAsString(content));
        } catch (Exception e) {
            item.setContent("{}");
        }

        item.setUpdatedAt(LocalDateTime.now());
        if (item.getId() == null) {
            item.setCreatedAt(LocalDateTime.now());
            knowledgeItemMapper.insert(item);
        } else {
            knowledgeItemMapper.updateById(item);
        }

        return new StyleSummaryResult(styleText, lastChapter, version, true);
    }

    @Override
    public List<KnowledgeItem> getAllForNovel(Long novelId, String itemType, int minStatus) {
        if (itemType != null && !itemType.isBlank()) {
            return knowledgeItemMapper.findByNovelAndType(novelId, itemType);
        }
        return knowledgeItemMapper.findByNovelAndMinStatus(novelId, minStatus);
    }

    @Override
    @Transactional
    public KnowledgeItem saveItem(KnowledgeItem item) {
        if (item.getId() == null) {
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            item.setVersion(item.getVersion() != null ? item.getVersion() : 1);
            knowledgeItemMapper.insert(item);
        } else {
            item.setUpdatedAt(LocalDateTime.now());
            item.setVersion((item.getVersion() != null ? item.getVersion() : 0) + 1);
            item.setStatus(2); // 已修改
            knowledgeItemMapper.updateById(item);
        }

        return item;
    }

    @Override
    @Transactional
    public KnowledgeItem updateItemStatus(Long id, int status) {
        KnowledgeItem item = knowledgeItemMapper.selectById(id);
        if (item == null) return null;
        item.setStatus(status);
        item.setUpdatedAt(LocalDateTime.now());
        knowledgeItemMapper.updateById(item);
        return item;
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        KnowledgeItem item = knowledgeItemMapper.selectById(id);
        if (item != null) {
            knowledgeItemMapper.deleteById(id);
        }
    }

    // --- private ---

    private String getExistingSummary(Long novelId) {
        List<KnowledgeItem> items = knowledgeItemMapper.findExistingSummary(novelId, DEFAULT_TOP);
        if (items.isEmpty()) return "（暂无已有知识）";

        StringBuilder sb = new StringBuilder();
        for (KnowledgeItem item : items) {
            sb.append("- [").append(item.getItemType()).append("] ")
                    .append(item.getName()).append("：")
                    .append(item.getSummary() != null ? item.getSummary() : "").append("\n");
        }
        return sb.toString();
    }

    private UpsertResult upsertEntity(Long novelId, Long chapterId, KnowledgeExtractor.ExtractedEntity entity) {
        KnowledgeItem existing = knowledgeItemMapper.findByNovelTypeName(
                novelId, entity.itemType(), entity.name());

        KnowledgeItem item;
        boolean isNew;

        if (existing != null) {
            Map<String, Object> mergedContent = mergeContent(entity.itemType(),
                    parseJsonContent(existing.getContent()), entity.content());
            try {
                existing.setContent(objectMapper.writeValueAsString(mergedContent));
            } catch (Exception e) {
                existing.setContent(existing.getContent());
            }
            existing.setSummary(entity.summary());
            existing.setConfidence(Math.max(existing.getConfidence(), entity.confidence()));
            existing.setVersion(existing.getVersion() + 1);
            existing.setSourceChapterOrder(entity.sourceChapterOrder());
            existing.setUpdatedAt(LocalDateTime.now());
            // 对应Python：已确认项更新→status=2(已修改)，否则保持待确认
            if (existing.getStatus() == 1) {
                existing.setStatus(2);
            }
            knowledgeItemMapper.updateById(existing);
            item = existing;
            isNew = false;
        } else {
            item = new KnowledgeItem();
            item.setNovelId(novelId);
            item.setItemType(entity.itemType());
            item.setName(entity.name());
            try {
                item.setContent(objectMapper.writeValueAsString(entity.content()));
            } catch (Exception e) {
                item.setContent("{}");
            }
            item.setSummary(entity.summary());
            item.setSourceChapterId(chapterId);
            item.setSourceChapterOrder(entity.sourceChapterOrder());
            item.setConfidence(entity.confidence());
            item.setVersion(1);
            item.setStatus(0); // 待确认
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            knowledgeItemMapper.insert(item);
            isNew = true;
        }

        return new UpsertResult(item, isNew);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mergeContent(String itemType,
                                              Map<String, Object> oldContent,
                                              Map<String, Object> newContent) {
        if (oldContent == null || oldContent.isEmpty()) return newContent;
        if (newContent == null || newContent.isEmpty()) return oldContent;

        Map<String, Object> merged = new HashMap<>(oldContent);

        if (KnowledgeExtractor.TYPE_CHARACTER.equals(itemType)) {
            for (String field : List.of("personality", "aliases", "abilities")) {
                List<String> oldList = objectToList(merged.get(field));
                List<String> newList = objectToList(newContent.get(field));
                List<String> mergedList = new ArrayList<>(oldList);
                for (String v : newList) {
                    if (!mergedList.contains(v)) mergedList.add(v);
                }
                merged.put(field, mergedList);
            }
            if (newContent.containsKey("relationships")) {
                Map<String, Object> oldRel = (Map<String, Object>) merged.get("relationships");
                Map<String, Object> newRel = (Map<String, Object>) newContent.get("relationships");
                if (oldRel == null) oldRel = new HashMap<>();
                if (newRel != null) oldRel.putAll(newRel);
                merged.put("relationships", oldRel);
            }
        } else if (KnowledgeExtractor.TYPE_PLOT.equals(itemType)) {
            List<Map<String, Object>> oldEvents = objectToMapList(merged.get("key_events"));
            List<Map<String, Object>> newEvents = objectToMapList(newContent.get("key_events"));
            for (Map<String, Object> ne : newEvents) {
                boolean exists = oldEvents.stream().anyMatch(
                        oe -> String.valueOf(oe.getOrDefault("description", ""))
                                .equals(String.valueOf(ne.getOrDefault("description", ""))));
                if (!exists) oldEvents.add(ne);
            }
            merged.put("key_events", oldEvents);
        } else {
            for (Map.Entry<String, Object> entry : newContent.entrySet()) {
                if (entry.getValue() != null && !String.valueOf(entry.getValue()).isBlank()) {
                    merged.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return merged;
    }

    private Map<String, Object> parseJsonContent(String content) {
        if (content == null || content.isBlank()) return new HashMap<>();
        try {
            return objectMapper.readValue(content, new TypeReference<>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> objectToList(Object obj) {
        if (obj instanceof List) return new ArrayList<>((List<String>) obj);
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> objectToMapList(Object obj) {
        if (obj instanceof List) return new ArrayList<>((List<Map<String, Object>>) obj);
        return new ArrayList<>();
    }

    private Map<String, Object> entitySummary(KnowledgeItem item) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", item.getId());
        summary.put("itemType", item.getItemType());
        summary.put("name", item.getName());
        summary.put("summary", item.getSummary());
        return summary;
    }

    private int getLastChapter(KnowledgeItem item) {
        try {
            Map<String, Object> content = parseJsonContent(item.getContent());
            Object lc = content.get("last_summarized_chapter");
            return lc instanceof Number ? ((Number) lc).intValue() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private String extractStyleTextFromContent(String content) {
        try {
            Map<String, Object> map = parseJsonContent(content);
            Object styleText = map.get("style_text");
            return styleText instanceof String s ? s : null;
        } catch (Exception e) {
            return null;
        }
    }

    private record UpsertResult(KnowledgeItem item, boolean isNew) {
    }
}
