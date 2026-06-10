package com.wang.aiserver.engine;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.wang.aiserver.extractor.PromptLoader;
import com.wang.aiserver.retriever.KnowledgeRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 续写引擎 - 对应 Python core/continuation_engine.py
 * 五阶段流水线：检索→规划→生成→审查→合规审查
 */
@Component
public class ContinuationEngine {

    private static final Logger log = LoggerFactory.getLogger(ContinuationEngine.class);

    private static final int MAX_CURRENT_CHARS = 2000;

    private final ChatClient textClient1;
    private final KnowledgeRetriever knowledgeRetriever;
    private final SelfReviewer selfReviewer;
    private final ContentReviewer contentReviewer;
    private final PromptLoader promptLoader;

    public ContinuationEngine(@Qualifier("textClient1") ChatClient textClient1,
                               KnowledgeRetriever knowledgeRetriever,
                               SelfReviewer selfReviewer,
                               ContentReviewer contentReviewer,
                               PromptLoader promptLoader) {
        this.textClient1 = textClient1;
        this.knowledgeRetriever = knowledgeRetriever;
        this.selfReviewer = selfReviewer;
        this.contentReviewer = contentReviewer;
        this.promptLoader = promptLoader;
    }

    /**
     * 生成续写 - 完整流水线
     */
    public ContinuationResult generate(Long novelId, String currentContent,
                                        List<String> chapterSummaries, String styleGuide,
                                        String authorInstructions, String selectedOutline,
                                        double temperature, int maxTokens) {
        // 截断当前内容
        String shortContent = truncateTail(currentContent, MAX_CURRENT_CHARS);

        // 阶段1: Wiki方式获取全部知识
        List<KnowledgeRetriever.RetrievedKnowledge> usedKnowledge =
                knowledgeRetriever.getKnowledge(novelId);
        String knowledgeContext = knowledgeRetriever.formatWiki(usedKnowledge);

        String chapterContext = buildChapterContext(chapterSummaries);

        // 阶段2: 大纲规划（如果没有提供）
        List<String> outlines;
        if (selectedOutline == null || selectedOutline.isBlank()) {
            outlines = plan(shortContent, knowledgeContext, chapterContext, styleGuide, authorInstructions);
            if (outlines.isEmpty()) {
                outlines = List.of("继续当前剧情发展");
            }
            selectedOutline = outlines.get(0);
        } else {
            outlines = List.of(selectedOutline);
        }

        // 阶段3: 生成续写
        String continuationText = generateText(shortContent, knowledgeContext, chapterContext,
                styleGuide, authorInstructions, selectedOutline, temperature, maxTokens);

        // 阶段4: 自我审查
        SelfReviewer.ReviewResult review = selfReviewer.review(continuationText, knowledgeContext,
                shortContent);

        // 阶段5: 内容合规审查
        ContentReviewer.ContentReviewResult contentReview = contentReviewer.review(
                continuationText, chapterContext);

        // 将 retrieved 转换为简单形式
        List<KnowledgeItemRef> knowledgeRefs = usedKnowledge.stream()
                .map(r -> new KnowledgeItemRef(r.id(), r.itemType(), r.name(), r.summary()))
                .toList();

        log.info("续写完成: qualityScore={}, knowledgeCount={}, contentSafe={}, riskLevel={}",
                review.score(), knowledgeRefs.size(), contentReview.safe(), contentReview.riskLevel());

        return new ContinuationResult(continuationText, outlines, knowledgeRefs,
                review.score(), review.issues(),
                contentReview.safe(), contentReview.riskScore(), contentReview.riskLevel(),
                contentReview.issues());
    }

    /**
     * 仅生成大纲
     */
    public List<String> plan(String currentContent, String knowledgeContext, String chapterContext,
                              String styleGuide, String authorInstructions) {
        try {
            String prompt = promptLoader.load("ai-continuation-plan")
                    .replace("${style_guide}", blankToDefault(styleGuide))
                    .replace("${knowledge_context}", blankToDefault(knowledgeContext))
                    .replace("${chapter_context}", blankToDefault(chapterContext))
                    .replace("${current_content}", blankToDefault(currentContent))
                    .replace("${author_instructions}", blankToDefault(authorInstructions));

            String response = textClient1.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return parseOutlines(response);
        } catch (Exception e) {
            log.error("大纲规划失败", e);
            return List.of("继续当前剧情发展");
        }
    }

    private String generateText(String currentContent, String knowledgeContext, String chapterContext,
                                 String styleGuide, String authorInstructions, String selectedOutline,
                                 double temperature, int maxTokens) {
        try {
            String prompt = promptLoader.load("ai-continuation-generate")
                    .replace("${style_guide}", blankToDefault(styleGuide))
                    .replace("${knowledge_context}", blankToDefault(knowledgeContext))
                    .replace("${chapter_context}", blankToDefault(chapterContext))
                    .replace("${current_content}", blankToDefault(currentContent))
                    .replace("${selected_outline}", blankToDefault(selectedOutline))
                    .replace("${author_instructions}", blankToDefault(authorInstructions));

            return textClient1.prompt()
                    .user(prompt)
                    .options(DashScopeChatOptions.builder()
                            .temperature(temperature)
                            .maxToken(maxTokens)
                            .build())
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("续写生成失败", e);
            return "";
        }
    }

    private List<String> parseOutlines(String response) {
        if (response == null || response.isBlank()) {
            return List.of("继续当前剧情发展");
        }

        List<String> outlines = new ArrayList<>();
        String[] lines = response.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("方向") || trimmed.contains("大纲") || trimmed.startsWith("主题")) {
                outlines.add(trimmed.replaceAll("^[#*\\d.、\\s]+", "").trim());
            }
        }

        if (outlines.isEmpty()) {
            String firstLine = lines.length > 0 ? lines[0].trim() : "继续当前剧情发展";
            outlines.add(firstLine);
        }

        return outlines;
    }

    private String buildChapterContext(List<String> chapterSummaries) {
        if (chapterSummaries == null || chapterSummaries.isEmpty()) {
            return "（暂无前文摘要）";
        }

        StringBuilder sb = new StringBuilder();
        int count = Math.min(chapterSummaries.size(), 5);
        for (int i = 0; i < count; i++) {
            sb.append("前情：").append(chapterSummaries.get(i)).append("\n");
        }
        return sb.toString().trim();
    }

    private String truncateTail(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(text.length() - maxLen);
    }

    private String blankToDefault(String text) {
        return text == null || text.isBlank() ? "（无）" : text;
    }

    /**
     * 续写结果
     */
    public record ContinuationResult(String continuationText, List<String> outlines,
                                      List<KnowledgeItemRef> usedKnowledge,
                                      double qualityScore, List<String> warnings,
                                      boolean contentSafe, double contentRiskScore,
                                      String contentRiskLevel, List<String> contentIssues) {
    }

    /**
     * 引用的知识项
     */
    public record KnowledgeItemRef(long id, String itemType, String name, String summary) {
    }
}
