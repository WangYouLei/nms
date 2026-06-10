package com.wang.aiserver.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wang.common.result.Result;
import com.wang.aiserver.service.AiWritingService;
import com.wang.pojo.dto.AiWritingDTO;
import com.wang.pojo.vo.AiWritingVO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiWritingServiceImpl implements AiWritingService {

    private static final int TYPE_CONTINUE = 1;
    private static final int TYPE_SUMMARY = 2;
    private static final int TYPE_CHARACTER_CHECK = 3;
    private static final int TYPE_TITLE_OPTIMIZE = 4;

    private static final Map<Integer, String> PROMPT_FILE_MAP = Map.of(
            TYPE_CONTINUE, "prompts/ai-writing-continue.txt",
            TYPE_SUMMARY, "prompts/ai-writing-summary.txt",
            TYPE_CHARACTER_CHECK, "prompts/ai-writing-character-check.txt",
            TYPE_TITLE_OPTIMIZE, "prompts/ai-writing-title-optimize.txt"
    );

    private final ChatClient textClient1;
    private final ObjectMapper objectMapper;
    private Map<Integer, String> promptTemplates;

    public AiWritingServiceImpl(@Qualifier("textClient1") ChatClient textClient1, ObjectMapper objectMapper) {
        this.textClient1 = textClient1;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        promptTemplates = PROMPT_FILE_MAP.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> loadPromptTemplate(e.getValue())
                ));
        log.info("AI写作助手提示词模板加载完成");
    }

    @Override
    public Result writingAssist(AiWritingDTO dto) {
        if (dto.getType() == null || !PROMPT_FILE_MAP.containsKey(dto.getType())) {
            return Result.error("无效的功能类型，支持：1-续写建议，2-章节摘要，3-角色一致性检查，4-标题/简介优化");
        }

        String validationResult = validateInput(dto);
        if (validationResult != null) {
            return Result.error(validationResult);
        }

        try {
            String prompt = buildPrompt(dto);
            String aiResponse = textClient1.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (aiResponse == null || aiResponse.isBlank()) {
                return Result.error("AI写作助手无响应，请稍后重试");
            }

            if (dto.getType() == TYPE_TITLE_OPTIMIZE) {
                return Result.success(parseTitleOptimizeResult(aiResponse));
            }

            return Result.success(AiWritingVO.of(dto.getType(), aiResponse.trim()));
        } catch (Exception e) {
            log.error("AI写作助手调用失败，type={}", dto.getType(), e);
            return Result.error("AI写作助手服务异常，请稍后重试");
        }
    }

    private String validateInput(AiWritingDTO dto) {
        switch (dto.getType()) {
            case TYPE_CONTINUE:
            case TYPE_SUMMARY:
            case TYPE_CHARACTER_CHECK:
                if (isBlank(dto.getContent())) {
                    return "请输入章节内容";
                }
                break;
            case TYPE_TITLE_OPTIMIZE:
                if (isBlank(dto.getTitle()) && isBlank(dto.getIntroduction())) {
                    return "请至少输入标题或简介";
                }
                break;
        }
        return null;
    }

    private String buildPrompt(AiWritingDTO dto) {
        String template = promptTemplates.get(dto.getType());
        if (template == null) {
            return "";
        }

        String content = truncate(dto.getContent(), 3000);
        String context = truncate(dto.getContext(), 3000);

        return template
                .replace("${content}", blankToDefault(content))
                .replace("${context}", blankToDefault(context))
                .replace("${title}", blankToDefault(dto.getTitle()))
                .replace("${introduction}", blankToDefault(dto.getIntroduction()));
    }

    private AiWritingVO parseTitleOptimizeResult(String aiResponse) {
        String jsonStr = aiResponse.trim();
        int jsonStart = jsonStr.indexOf('{');
        int jsonEnd = jsonStr.lastIndexOf('}');
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            jsonStr = jsonStr.substring(jsonStart, jsonEnd + 1);
        }

        try {
            Map<String, Object> result = objectMapper.readValue(jsonStr, new TypeReference<>() {});
            @SuppressWarnings("unchecked")
            List<String> titles = result.get("titles") != null
                    ? ((List<String>) result.get("titles"))
                    : Collections.emptyList();
            String introduction = result.get("introduction") != null
                    ? result.get("introduction").toString()
                    : "";
            return AiWritingVO.ofTitleAndIntro(titles, introduction);
        } catch (Exception e) {
            log.warn("AI标题优化结果JSON解析失败，返回原始文本", e);
            return AiWritingVO.ofTitleAndIntro(Collections.emptyList(), aiResponse.trim());
        }
    }

    private String loadPromptTemplate(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            log.warn("加载提示词模板失败：{}", path, e);
            return "";
        }
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return null;
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    private String blankToDefault(String text) {
        return isBlank(text) ? "（未提供）" : text;
    }
}
