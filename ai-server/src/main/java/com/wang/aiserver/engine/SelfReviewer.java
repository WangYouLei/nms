package com.wang.aiserver.engine;

import com.wang.aiserver.extractor.PromptLoader;
import com.wang.aiserver.retriever.KnowledgeRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 自我审查引擎 - 对应 Python core/self_review.py
 * 对生成的续写内容进行质量评分和问题检测
 */
@Component
public class SelfReviewer {

    private static final Logger log = LoggerFactory.getLogger(SelfReviewer.class);

    private static final Pattern SCORE_PATTERN = Pattern.compile("(\\d+)\\s*分?");
    private static final int MAX_CONTENT_CHARS = 3000;

    private final ChatClient textClient1;
    private final PromptLoader promptLoader;

    public SelfReviewer(@Qualifier("textClient1") ChatClient textClient1,
                         PromptLoader promptLoader) {
        this.textClient1 = textClient1;
        this.promptLoader = promptLoader;
    }

    /**
     * 审查生成的续写内容
     */
    public ReviewResult review(String generatedContent, String knowledgeContext, String chapterContext) {
        try {
            String truncated = generatedContent.length() > MAX_CONTENT_CHARS
                    ? generatedContent.substring(0, MAX_CONTENT_CHARS) : generatedContent;

            String prompt = promptLoader.load("ai-self-review")
                    .replace("${generated_content}", truncated)
                    .replace("${knowledge_context}", blankToDefault(knowledgeContext))
                    .replace("${chapter_context}", blankToDefault(chapterContext));

            String response = textClient1.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return parseReview(response);
        } catch (Exception e) {
            log.error("自我审查失败", e);
            return new ReviewResult(70.0, List.of("自我审查服务异常，默认评分为70分"), "");
        }
    }

    private ReviewResult parseReview(String response) {
        if (response == null || response.isBlank()) {
            return new ReviewResult(70.0, List.of("审查返回空结果"), "");
        }

        double score = extractScore(response);
        List<String> issues = extractIssues(response);
        String suggestions = extractSuggestions(response);

        score = Math.max(0, Math.min(100, score));

        return new ReviewResult(score, issues, suggestions);
    }

    private double extractScore(String text) {
        // 查找 "质量评分：85" 或 "评分：85分" 等格式
        Matcher matcher = SCORE_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        // 按行查找
        for (String line : text.split("\n")) {
            if (line.contains("评分") || line.contains("分数")) {
                Matcher m = SCORE_PATTERN.matcher(line);
                if (m.find()) {
                    try {
                        return Double.parseDouble(m.group(1));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return 70.0;
    }

    private List<String> extractIssues(String text) {
        List<String> issues = new ArrayList<>();
        for (String line : text.split("\n")) {
            line = line.trim();
            // 匹配 "- [类型: 描述]" 格式
            if (line.startsWith("-") && line.contains("：") || line.startsWith("-") && line.contains(":")) {
                issues.add(line.replaceFirst("^[-•\\s]+", "").trim());
            }
        }
        return issues;
    }

    private String extractSuggestions(String text) {
        StringBuilder sb = new StringBuilder();
        boolean inSuggestions = false;
        for (String line : text.split("\n")) {
            if (line.contains("建议") || line.contains("修改")) {
                inSuggestions = true;
            }
            if (inSuggestions && !line.trim().isEmpty()) {
                sb.append(line.trim()).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private String blankToDefault(String text) {
        return text == null || text.isBlank() ? "（无）" : text;
    }

    /**
     * 审查结果
     */
    public record ReviewResult(double score, List<String> issues, String suggestions) {
    }
}
