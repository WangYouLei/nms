package com.wang.aiserver.retriever;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wang.aiserver.mapper.KnowledgeItemMapper;
import com.wang.pojo.entity.KnowledgeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识检索器 - 对应 Python core/knowledge_retriever.py
 * Wiki方式：从MySQL取全部知识，格式化为结构化文档注入Prompt
 */
@Component
public class KnowledgeRetriever {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeRetriever.class);

    private static final Map<String, String> TYPE_LABELS = Map.of(
            "character", "角色设定",
            "setting", "场景设定",
            "plot", "剧情线索",
            "theme", "主题",
            "item", "关键物品",
            "style_summary", "写作风格"
    );

    private static final List<String> TYPE_ORDER = List.of(
            "character", "setting", "plot", "theme", "item", "style_summary"
    );

    private static final int MAX_PLOT_ITEMS = 20;
    private static final int MAX_THEME_ITEMS = 10;
    private static final int MAX_ITEM_ITEMS = 10;
    private static final int WIKI_MAX_CHARS = 8000;

    private final KnowledgeItemMapper knowledgeItemMapper;
    private final ObjectMapper objectMapper;

    public KnowledgeRetriever(KnowledgeItemMapper knowledgeItemMapper,
                               ObjectMapper objectMapper) {
        this.knowledgeItemMapper = knowledgeItemMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取小说的全部知识项
     */
    public List<RetrievedKnowledge> getKnowledge(long novelId) {
        return getKnowledge(novelId, null, 0);
    }

    /**
     * 获取小说的知识项（可按类型和状态过滤）
     */
    public List<RetrievedKnowledge> getKnowledge(long novelId, List<String> itemTypes, int minStatus) {
        List<KnowledgeItem> items;

        if (itemTypes != null && !itemTypes.isEmpty()) {
            items = new ArrayList<>();
            for (String type : itemTypes) {
                items.addAll(knowledgeItemMapper.findByNovelAndType(novelId, type));
            }
        } else if (minStatus > 0) {
            items = knowledgeItemMapper.findByNovelAndMinStatus(novelId, minStatus);
        } else {
            items = knowledgeItemMapper.findByNovelAndMinStatus(novelId, 0);
        }

        return items.stream()
                .map(this::toRetrievedKnowledge)
                .collect(Collectors.toList());
    }

    /**
     * 格式化为Wiki文档
     */
    public String formatWiki(List<RetrievedKnowledge> items) {
        if (items == null || items.isEmpty()) {
            return "暂无相关知识条目。";
        }

        Map<String, List<RetrievedKnowledge>> byType = new LinkedHashMap<>();
        for (RetrievedKnowledge item : items) {
            byType.computeIfAbsent(item.itemType(), k -> new ArrayList<>()).add(item);
        }

        List<String> sections = new ArrayList<>();
        for (String typeKey : TYPE_ORDER) {
            List<RetrievedKnowledge> typeItems = byType.get(typeKey);
            if (typeItems == null) continue;

            List<RetrievedKnowledge> limited = limitItems(typeKey, typeItems);
            String section = formatSection(typeKey, limited);
            if (section != null && !section.isBlank()) {
                sections.add(section);
            }
        }

        String wiki = String.join("\n\n", sections);
        if (wiki.length() > WIKI_MAX_CHARS) {
            wiki = wiki.substring(0, WIKI_MAX_CHARS) + "\n\n[...知识库内容过长，已截断]";
        }
        return wiki;
    }

    private List<RetrievedKnowledge> limitItems(String typeKey, List<RetrievedKnowledge> items) {
        int limit = switch (typeKey) {
            case "plot" -> MAX_PLOT_ITEMS;
            case "theme" -> MAX_THEME_ITEMS;
            case "item" -> MAX_ITEM_ITEMS;
            default -> Integer.MAX_VALUE;
        };
        if (items.size() > limit) {
            return items.subList(0, limit);
        }
        return items;
    }

    private String formatSection(String typeKey, List<RetrievedKnowledge> items) {
        String label = TYPE_LABELS.getOrDefault(typeKey, typeKey);
        List<String> lines = new ArrayList<>();
        lines.add("## " + label);

        for (RetrievedKnowledge item : items) {
            if ("style_summary".equals(typeKey)) {
                Object styleText = item.content().get("style_text");
                if (styleText instanceof String s && !s.isBlank()) {
                    lines.add(s);
                } else if (item.summary() != null && !item.summary().isBlank()) {
                    lines.add(item.summary());
                }
                continue;
            }
            lines.add("### " + item.name());
            String detail = formatItemDetail(typeKey, item);
            if (detail != null && !detail.isBlank()) {
                lines.add(detail);
            }
        }

        return String.join("\n", lines);
    }

    private String formatItemDetail(String typeKey, RetrievedKnowledge item) {
        Map<String, Object> c = item.content();
        if (c == null || c.isEmpty()) return "";

        List<String> parts = new ArrayList<>();

        if ("character".equals(typeKey)) {
            addIfPresent(parts, c, "gender", "性别");
            addIfPresent(parts, c, "age", "年龄");
            addListIfPresent(parts, c, "personality", "性格");
            addIfPresent(parts, c, "appearance", "外貌");
            addIfPresent(parts, c, "background", "背景");
            addListIfPresent(parts, c, "abilities", "能力");
            if (c.get("relationships") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> rels = (Map<String, Object>) c.get("relationships");
                String relStr = rels.entrySet().stream()
                        .map(e -> e.getKey() + "(" + e.getValue() + ")")
                        .collect(Collectors.joining("、"));
                if (!relStr.isBlank()) parts.add("关系：" + relStr);
            }
        } else if ("setting".equals(typeKey)) {
            addIfPresent(parts, c, "type", "类型");
            addIfPresent(parts, c, "description", "描述");
            if (c.get("attributes") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> attrs = (Map<String, Object>) c.get("attributes");
                String attrStr = attrs.entrySet().stream()
                        .map(e -> e.getKey() + ": " + e.getValue())
                        .collect(Collectors.joining("、"));
                if (!attrStr.isBlank()) parts.add("特点：" + attrStr);
            }
        } else if ("plot".equals(typeKey)) {
            addIfPresent(parts, c, "type", "类型");
            addIfPresent(parts, c, "description", "描述");
            addIfPresent(parts, c, "status", "状态");
            addListIfPresent(parts, c, "related_characters", "相关角色");
        } else if ("theme".equals(typeKey) || "item".equals(typeKey)) {
            addIfPresent(parts, c, "description", "描述");
        }

        return String.join("；", parts);
    }

    private void addIfPresent(List<String> parts, Map<String, Object> c, String key, String label) {
        Object val = c.get(key);
        if (val != null && !String.valueOf(val).isBlank() && !"未知".equals(String.valueOf(val))) {
            parts.add(label + "：" + val);
        }
    }

    @SuppressWarnings("unchecked")
    private void addListIfPresent(List<String> parts, Map<String, Object> c, String key, String label) {
        Object val = c.get(key);
        if (val instanceof List list && !list.isEmpty()) {
            parts.add(label + "：" + String.join("、", list.stream().map(Object::toString).toList()));
        }
    }

    private RetrievedKnowledge toRetrievedKnowledge(KnowledgeItem item) {
        Map<String, Object> content = safeJsonParse(item.getContent());
        return new RetrievedKnowledge(
                item.getId(),
                item.getItemType(),
                item.getName(),
                content,
                item.getSummary() != null ? item.getSummary() : ""
        );
    }

    private Map<String, Object> safeJsonParse(String content) {
        if (content == null || content.isBlank()) return new HashMap<>();
        try {
            return objectMapper.readValue(content, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of("raw", content);
        }
    }

    /**
     * 检索到的知识项
     */
    public record RetrievedKnowledge(long id, String itemType, String name,
                                      Map<String, Object> content, String summary) {
    }
}
