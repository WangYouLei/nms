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
 * 文本润色引擎 - 对应 Python core/polishing_engine.py
 * 6维度润色 + 内容合规审查
 */
@Component
public class PolishingEngine {

    private static final Logger log = LoggerFactory.getLogger(PolishingEngine.class);

    private static final int MAX_TEXT_CHARS = 3000;

    private final ChatClient textClient1;
    private final PromptLoader promptLoader;
    private final ContentReviewer contentReviewer;

    public PolishingEngine(@Qualifier("textClient1") ChatClient textClient1,
                            PromptLoader promptLoader,
                            ContentReviewer contentReviewer) {
        this.textClient1 = textClient1;
        this.promptLoader = promptLoader;
        this.contentReviewer = contentReviewer;
    }

    /**
     * 润色文本
     */
    public PolishResult polish(String text, List<String> aspects, String styleGuide,
                                String customInstruction, boolean preserveLength,
                                boolean generateLonger, double temperature) {
        String truncated = text.length() > MAX_TEXT_CHARS
                ? text.substring(0, MAX_TEXT_CHARS) : text;

        String aspectsStr = buildAspectsString(aspects);
        String lengthInstruction = buildLengthInstruction(preserveLength, generateLonger);

        try {
            String prompt = promptLoader.load("ai-polishing")
                    .replace("${style_guide}", blankToDefault(styleGuide))
                    .replace("${aspects}", aspectsStr)
                    .replace("${text}", truncated)
                    .replace("${custom_instruction}", blankToDefault(customInstruction))
                    .replace("${preserve_length_instruction}", lengthInstruction);

            String response = textClient1.prompt()
                    .user(prompt)
                    .call()
                    .content();

            PolishResult result = parseResponse(response);

            // 内容合规审查
            ContentReviewer.ContentReviewResult contentReview = contentReviewer.review(
                    result.polishedText, styleGuide);
            result = new PolishResult(result.polishedText, result.changes, result.summary,
                    contentReview.safe(), contentReview.riskScore(),
                    contentReview.riskLevel(), contentReview.issues());

            return result;
        } catch (Exception e) {
            log.error("文本润色失败", e);
            return new PolishResult(text, List.of(), "润色服务异常，返回原文",
                    true, 100.0, "safe", List.of());
        }
    }

    private String buildAspectsString(List<String> aspects) {
        if (aspects == null || aspects.isEmpty()) {
            return "语法修正、风格润色、连贯性改善、描写增强、对话优化";
        }

        StringBuilder sb = new StringBuilder();
        for (String aspect : aspects) {
            sb.append(getAspectLabel(aspect)).append("、");
        }
        return sb.substring(0, sb.length() - 1);
    }

    private String getAspectLabel(String aspect) {
        return switch (aspect) {
            case "grammar" -> "语法修正";
            case "style" -> "风格润色";
            case "coherence" -> "连贯性改善";
            case "description" -> "描写增强";
            case "dialogue" -> "对话优化";
            case "custom" -> "自定义要求";
            default -> aspect;
        };
    }

    private String buildLengthInstruction(boolean preserveLength, boolean generateLonger) {
        if (generateLonger) {
            return "可以适当扩展细节，使内容更加丰富，字数可以增加到原文的1.5倍左右。";
        }
        if (preserveLength) {
            return "尽量保持原文的段落结构和字数，不要过度扩展。";
        }
        return "可以在合理范围内调整字数和结构。";
    }

    private PolishResult parseResponse(String response) {
        if (response == null || response.isBlank()) {
            return new PolishResult("", List.of(), "润色返回空结果",
                    true, 100.0, "safe", List.of());
        }

        String polishedText = response;
        String changeSection = "";
        List<PolishChange> changes = new ArrayList<>();

        int polishedStart = response.indexOf("【润色后】");
        int changeStart = response.indexOf("【修改说明】");

        if (polishedStart >= 0 && changeStart >= 0 && changeStart > polishedStart) {
            polishedText = response.substring(polishedStart + 6, changeStart).trim();
            changeSection = response.substring(changeStart + 6).trim();
        } else if (polishedStart >= 0) {
            polishedText = response.substring(polishedStart + 6).trim();
        }

        if (!changeSection.isEmpty()) {
            for (String line : changeSection.split("\n")) {
                line = line.trim();
                if (line.startsWith("-") && line.contains("：")) {
                    String content = line.replaceFirst("^[-•\\s]+", "").trim();
                    int colonIdx = content.indexOf("：");
                    if (colonIdx > 0) {
                        String type = content.substring(0, colonIdx);
                        String desc = content.substring(colonIdx + 1);
                        changes.add(new PolishChange(type, "", "", desc));
                    }
                }
            }
        }

        String summary = !changeSection.isBlank()
                ? changeSection.substring(0, Math.min(200, changeSection.length()))
                : "根据" + changes.size() + "个维度完成润色";

        return new PolishResult(polishedText, changes, summary,
                true, 100.0, "safe", List.of());
    }

    private String blankToDefault(String text) {
        return text == null || text.isBlank() ? "（无）" : text;
    }

    /**
     * 润色结果
     */
    public record PolishResult(String polishedText, List<PolishChange> changes, String summary,
                                boolean contentSafe, double contentRiskScore,
                                String contentRiskLevel, List<String> contentIssues) {
    }

    /**
     * 润色变更记录
     */
    public record PolishChange(String type, String original, String polished, String description) {
    }
}
