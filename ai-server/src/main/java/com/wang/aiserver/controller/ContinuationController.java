package com.wang.aiserver.controller;

import com.wang.aiserver.engine.ContinuationEngine;
import com.wang.aiserver.retriever.KnowledgeRetriever;
import com.wang.aiserver.service.KnowledgeService;
import com.wang.aiserver.service.StyleSummaryResult;
import com.wang.common.result.Result;
import com.wang.pojo.dto.ContinuationGenerateDTO;
import com.wang.pojo.dto.ContinuationPlanDTO;
import com.wang.pojo.vo.ContinuationGenerateVO;
import com.wang.pojo.vo.ContinuationPlanVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/continuation")
public class ContinuationController {

    private final ContinuationEngine continuationEngine;
    private final KnowledgeService knowledgeService;
    private final KnowledgeRetriever knowledgeRetriever;

    public ContinuationController(ContinuationEngine continuationEngine,
                                   KnowledgeService knowledgeService,
                                   KnowledgeRetriever knowledgeRetriever) {
        this.continuationEngine = continuationEngine;
        this.knowledgeService = knowledgeService;
        this.knowledgeRetriever = knowledgeRetriever;
    }

    @PostMapping("/plan")
    public Result plan(@RequestBody ContinuationPlanDTO dto) {
        log.info("续写大纲规划：novelId={}", dto.getNovelId());

        if (dto.getNovelId() == null) {
            return Result.error("小说ID不能为空");
        }
        if (dto.getCurrentContent() == null || dto.getCurrentContent().isBlank()) {
            return Result.error("当前章节内容不能为空");
        }

        List<String> outlines = continuationEngine.plan(
                dto.getCurrentContent(),
                getKnowledgeContext(dto.getNovelId()),
                buildChapterContext(dto.getChapterSummaries()),
                getStyleGuide(dto.getNovelId()),
                dto.getAuthorInstructions());

        ContinuationPlanVO vo = new ContinuationPlanVO();
        vo.setOutlines(outlines);
        return Result.success(vo);
    }

    @PostMapping("/generate")
    public Result generate(@RequestBody ContinuationGenerateDTO dto) {
        log.info("续写生成：novelId={}", dto.getNovelId());

        if (dto.getNovelId() == null) {
            return Result.error("小说ID不能为空");
        }
        if (dto.getCurrentContent() == null || dto.getCurrentContent().isBlank()) {
            return Result.error("当前章节内容不能为空");
        }

        String styleGuide = "";
        StyleSummaryResult styleResult = knowledgeService.getStyleSummary(dto.getNovelId());
        if (styleResult.isExists()) {
            styleGuide = styleResult.getStyleText();
        }

        double temperature = dto.getTemperature() != null ? dto.getTemperature() : 0.7;
        int maxTokens = dto.getMaxTokens() != null ? dto.getMaxTokens() : 2048;

        ContinuationEngine.ContinuationResult result = continuationEngine.generate(
                dto.getNovelId(),
                dto.getCurrentContent(),
                dto.getChapterSummaries(),
                styleGuide,
                dto.getAuthorInstructions(),
                dto.getSelectedOutline(),
                temperature,
                maxTokens);

        ContinuationGenerateVO vo = new ContinuationGenerateVO();
        vo.setContinuationText(result.continuationText());
        vo.setOutlines(result.outlines());
        vo.setQualityScore(result.qualityScore());
        vo.setWarnings(result.warnings());
        vo.setContentSafe(result.contentSafe());
        vo.setContentRiskScore(result.contentRiskScore());
        vo.setContentRiskLevel(result.contentRiskLevel());
        vo.setContentIssues(result.contentIssues());

        // 转换知识引用
        List<ContinuationGenerateVO.KnowledgeItemRef> refs = new ArrayList<>();
        for (ContinuationEngine.KnowledgeItemRef ref : result.usedKnowledge()) {
            ContinuationGenerateVO.KnowledgeItemRef voRef = new ContinuationGenerateVO.KnowledgeItemRef();
            voRef.setId(ref.id());
            voRef.setItemType(ref.itemType());
            voRef.setName(ref.name());
            voRef.setSummary(ref.summary());
            refs.add(voRef);
        }
        vo.setUsedKnowledge(refs);

        return Result.success(vo);
    }

    private String getKnowledgeContext(Long novelId) {
        if (novelId == null) return "（无）";
        var knowledge = knowledgeRetriever.getKnowledge(novelId);
        return knowledgeRetriever.formatWiki(knowledge);
    }

    private String getStyleGuide(Long novelId) {
        if (novelId == null) return "（无）";
        StyleSummaryResult styleResult = knowledgeService.getStyleSummary(novelId);
        return styleResult.isExists() ? styleResult.getStyleText() : "（无）";
    }

    private String buildChapterContext(List<String> chapterSummaries) {
        if (chapterSummaries == null || chapterSummaries.isEmpty()) return "（暂无前文摘要）";
        StringBuilder sb = new StringBuilder();
        int count = Math.min(chapterSummaries.size(), 5);
        for (int i = 0; i < count; i++) {
            sb.append("前情：").append(chapterSummaries.get(i)).append("\n");
        }
        return sb.toString().trim();
    }
}
