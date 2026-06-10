package com.wang.aiserver.extractor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 知识提取器 - 对应 Python core/knowledge_extractor.py
 * 从小说章节文本中提取结构化的知识实体（角色、设定、剧情、主题、物品）
 */
@Component
public class KnowledgeExtractor {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeExtractor.class);

    public static final String TYPE_CHARACTER = "character";
    public static final String TYPE_SETTING = "setting";
    public static final String TYPE_PLOT = "plot";
    public static final String TYPE_THEME = "theme";
    public static final String TYPE_ITEM = "item";

    public static final String[] ALL_TYPES = {TYPE_CHARACTER, TYPE_SETTING, TYPE_PLOT, TYPE_THEME, TYPE_ITEM};

    private final ChatClient textClient1;
    private final ObjectMapper objectMapper;
    private final PromptLoader promptLoader;

    public KnowledgeExtractor(@Qualifier("textClient1") ChatClient textClient1,
                               ObjectMapper objectMapper,
                               PromptLoader promptLoader) {
        this.textClient1 = textClient1;
        this.objectMapper = objectMapper;
        this.promptLoader = promptLoader;
    }

    /**
     * 从章节文本中提取知识实体
     */
    public List<ExtractedEntity> extractFromChapter(String chapterText, String novelInfo,
                                                     String existingKnowledge, int chapterOrder) {
        // 截断长文本
        String truncated = truncate(chapterText, 5000);

        log.info("开始知识提取，章节长度={}", truncated.length());
        return extractFromChunk(truncated, novelInfo, existingKnowledge, chapterOrder);
    }

    private List<ExtractedEntity> extractFromChunk(String chapterText, String novelInfo,
                                                    String existingKnowledge, int chapterOrder) {
        try {
            String prompt = promptLoader.load("ai-extraction")
                    .replace("${chapter_text}", chapterText)
                    .replace("${novel_info}", blankToDefault(novelInfo))
                    .replace("${existing_knowledge}", blankToDefault(existingKnowledge));

            String response = textClient1.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                log.warn("知识提取返回空结果");
                return List.of();
            }

            return parseResponse(response, chapterOrder);
        } catch (Exception e) {
            log.error("知识提取失败", e);
            return List.of();
        }
    }

    private List<ExtractedEntity> parseResponse(String response, int chapterOrder) {
        List<ExtractedEntity> entities = new ArrayList<>();

        // 提取 JSON 部分
        String jsonStr = extractJson(response);
        if (jsonStr == null) {
            log.warn("无法从响应中提取 JSON: {}", response.substring(0, Math.min(200, response.length())));
            return entities;
        }

        try {
            Map<String, List<Map<String, Object>>> data = objectMapper.readValue(
                    jsonStr, new TypeReference<>() {});

            for (String type : ALL_TYPES) {
                List<Map<String, Object>> items = data.getOrDefault(type + "s", List.of());
                for (Map<String, Object> item : items) {
                    String name = String.valueOf(item.getOrDefault("name", ""));
                    if (name.isBlank()) continue;

                    double confidence = estimateConfidence(item);
                    String summary = buildSummary(type, item);

                    entities.add(new ExtractedEntity(type, name, item, summary, confidence,
                            chapterOrder));
                }
            }
        } catch (Exception e) {
            log.error("解析知识提取响应失败", e);
        }

        return entities;
    }

    private String extractJson(String text) {
        // 尝试找到外层的 { }
        int start = text.indexOf('{');
        if (start < 0) return null;

        int braceDepth = 0;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') braceDepth++;
            else if (c == '}') {
                braceDepth--;
                if (braceDepth == 0) {
                    return text.substring(start, i + 1);
                }
            }
        }
        return null;
    }

    private double estimateConfidence(Map<String, Object> item) {
        int filledFields = 0;
        int totalFields = 0;
        for (Object value : item.values()) {
            totalFields++;
            if (value != null && !String.valueOf(value).isBlank()
                    && !"[]".equals(String.valueOf(value))
                    && !"{}".equals(String.valueOf(value))) {
                filledFields++;
            }
        }
        double ratio = totalFields > 0 ? (double) filledFields / totalFields : 0.5;
        return Math.min(0.3 + ratio * 0.65, 0.95);
    }

    private String buildSummary(String type, Map<String, Object> item) {
        String name = String.valueOf(item.getOrDefault("name", ""));
        return switch (type) {
            case TYPE_CHARACTER -> name + " - " + item.getOrDefault("personality", "");
            case TYPE_SETTING -> name + "(" + item.getOrDefault("type", "") + ") - "
                    + truncate(String.valueOf(item.getOrDefault("description", "")), 80);
            case TYPE_PLOT -> name + "(" + item.getOrDefault("type", "") + ") - "
                    + truncate(String.valueOf(item.getOrDefault("description", "")), 80);
            case TYPE_THEME -> name + " - " + truncate(String.valueOf(item.getOrDefault("description", "")), 80);
            case TYPE_ITEM -> name + " - " + truncate(String.valueOf(item.getOrDefault("description", "")), 80);
            default -> name;
        };
    }

    /**
     * 提取写作风格总结 - 对应 Python KnowledgeExtractor.extract_style_summary()
     */
    public String extractStyleSummary(String chapterSamples, String existingStyle) {
        try {
            String prompt = promptLoader.load("ai-style-extraction")
                    .replace("${existing_style}", blankToDefault(existingStyle))
                    .replace("${chapter_samples}", chapterSamples);

            String response = textClient1.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                log.warn("风格提取返回空结果");
                return existingStyle != null ? existingStyle : "";
            }

            return response.trim();
        } catch (Exception e) {
            log.error("风格提取失败", e);
            return existingStyle != null ? existingStyle : "";
        }
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }

    private String blankToDefault(String text) {
        return text == null || text.isBlank() ? "（无）" : text;
    }

    /**
     * 提取的实体
     */
    public record ExtractedEntity(String itemType, String name, Map<String, Object> content,
                                   String summary, double confidence, int sourceChapterOrder) {
    }
}
