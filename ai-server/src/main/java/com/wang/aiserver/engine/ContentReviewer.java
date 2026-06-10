package com.wang.aiserver.engine;

import com.wang.aiserver.extractor.PromptLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * AI内容合规审查引擎 - 对应 Python core/content_review.py
 * 对AI生成的文本进行内容合规审查（色情、政治敏感、社会价值观等）
 */
@Component
public class ContentReviewer {

    private static final Logger log = LoggerFactory.getLogger(ContentReviewer.class);

    private static final int MAX_CONTENT_CHARS = 3000;

    private final ChatClient textClient1;
    private final PromptLoader promptLoader;

    public ContentReviewer(@Qualifier("textClient1") ChatClient textClient1,
                            PromptLoader promptLoader) {
        this.textClient1 = textClient1;
        this.promptLoader = promptLoader;
    }

    /**
     * 审查生成的内容是否合规
     */
    public ContentReviewResult review(String generatedContent, String chapterContext) {
        try {
            String truncated = generatedContent.length() > MAX_CONTENT_CHARS
                    ? generatedContent.substring(0, MAX_CONTENT_CHARS) : generatedContent;

            String prompt = promptLoader.load("ai-content-review")
                    .replace("${generated_content}", truncated)
                    .replace("${chapter_context}", chapterContext != null ? chapterContext : "");

            String response = textClient1.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return parseReview(response);
        } catch (Exception e) {
            log.error("内容合规审查失败", e);
            return new ContentReviewResult(true, 100.0, "safe", List.of("无法完成内容合规审查，请人工检查"));
        }
    }

    private ContentReviewResult parseReview(String response) {
        if (response == null || response.isBlank()) {
            return new ContentReviewResult(true, 100.0, "safe", List.of());
        }

        double riskScore = 100.0;
        String riskLevel = "safe";
        List<String> issues = new ArrayList<>();

        for (String line : response.split("\n")) {
            line = line.trim();
            if (line.contains("安全评分") || line.contains("评分")) {
                riskScore = extractScore(line);
            } else if (line.contains("风险等级") || line.contains("等级")) {
                riskLevel = extractRiskLevel(line);
            } else if (line.contains("问题描述")) {
                String content = extractAfterColon(line);
                if (content != null && !content.isBlank() && !"无".equals(content) && !content.contains("无违规")) {
                    issues.add(content);
                }
            } else if (line.startsWith("-") && line.contains("：")) {
                issues.add(line.replaceFirst("^[-•\\s]+", "").trim());
            }
        }

        riskScore = Math.max(0, Math.min(100, riskScore));
        boolean safe = "safe".equals(riskLevel) || "mild".equals(riskLevel);

        return new ContentReviewResult(safe, riskScore, riskLevel, issues);
    }

    private double extractScore(String line) {
        String normalized = line.replace("：", ":");
        for (String part : normalized.split(":")) {
            String trimmed = part.trim().replaceAll("分$", "");
            try {
                return Double.parseDouble(trimmed);
            } catch (NumberFormatException ignored) {
            }
        }
        return 100.0;
    }

    private String extractRiskLevel(String line) {
        String lower = line.toLowerCase();
        if (lower.contains("blocked") || lower.contains("拦截")) return "blocked";
        if (lower.contains("warning") || lower.contains("警告")) return "warning";
        if (lower.contains("mild") || lower.contains("轻微")) return "mild";
        return "safe";
    }

    private String extractAfterColon(String line) {
        String normalized = line.replaceFirst("：", ":");
        int idx = normalized.indexOf(":");
        if (idx >= 0 && idx < normalized.length() - 1) {
            return normalized.substring(idx + 1).trim();
        }
        return null;
    }

    /**
     * 内容合规审查结果
     */
    public record ContentReviewResult(boolean safe, double riskScore, String riskLevel,
                                       List<String> issues) {
    }
}
