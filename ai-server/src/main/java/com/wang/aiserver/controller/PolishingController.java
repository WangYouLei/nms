package com.wang.aiserver.controller;

import com.wang.aiserver.engine.PolishingEngine;
import com.wang.aiserver.service.KnowledgeService;
import com.wang.aiserver.service.StyleSummaryResult;
import com.wang.common.result.Result;
import com.wang.pojo.dto.PolishRequestDTO;
import com.wang.pojo.vo.PolishResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/polishing")
public class PolishingController {

    private final PolishingEngine polishingEngine;
    private final KnowledgeService knowledgeService;

    public PolishingController(PolishingEngine polishingEngine,
                                KnowledgeService knowledgeService) {
        this.polishingEngine = polishingEngine;
        this.knowledgeService = knowledgeService;
    }

    @PostMapping("/polish")
    public Result polish(@RequestBody PolishRequestDTO dto) {
        log.info("文本润色请求");

        if (dto.getText() == null || dto.getText().isBlank()) {
            return Result.error("待润色文本不能为空");
        }

        String styleGuide = "";
        if (dto.getNovelId() != null) {
            StyleSummaryResult styleResult = knowledgeService.getStyleSummary(dto.getNovelId());
            if (styleResult.isExists()) {
                styleGuide = styleResult.getStyleText();
            }
        }

        boolean preserveLength = dto.getPreserveLength() == null || dto.getPreserveLength();
        boolean generateLonger = dto.getGenerateLonger() != null && dto.getGenerateLonger();
        double temperature = dto.getTemperature() != null ? dto.getTemperature() : 0.3;

        PolishingEngine.PolishResult result = polishingEngine.polish(
                dto.getText(),
                dto.getAspects(),
                styleGuide,
                dto.getCustomInstruction(),
                preserveLength,
                generateLonger,
                temperature);

        PolishResultVO vo = new PolishResultVO();
        vo.setPolishedText(result.polishedText());
        vo.setSummary(result.summary());
        vo.setContentSafe(result.contentSafe());
        vo.setContentRiskScore(result.contentRiskScore());
        vo.setContentRiskLevel(result.contentRiskLevel());
        vo.setContentIssues(result.contentIssues());

        List<PolishResultVO.PolishChangeItem> changes = new ArrayList<>();
        for (PolishingEngine.PolishChange change : result.changes()) {
            PolishResultVO.PolishChangeItem item = new PolishResultVO.PolishChangeItem();
            item.setType(change.type());
            item.setOriginal(change.original());
            item.setPolished(change.polished());
            item.setDescription(change.description());
            changes.add(item);
        }
        vo.setChanges(changes);

        return Result.success(vo);
    }
}
