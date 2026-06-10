package com.wang.aiserver.controller;

import com.wang.aiserver.service.KnowledgeService;
import com.wang.aiserver.service.StyleSummaryResult;
import com.wang.common.result.Result;
import com.wang.pojo.dto.StyleSummaryRefreshDTO;
import com.wang.pojo.dto.StyleSummaryUpdateDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/knowledge")
public class StyleSummaryController {

    private final KnowledgeService knowledgeService;

    public StyleSummaryController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @GetMapping("/style-summary")
    public Result getStyleSummary(@RequestParam Long novelId) {
        StyleSummaryResult result = knowledgeService.getStyleSummary(novelId);

        Map<String, Object> data = new HashMap<>();
        data.put("novel_id", novelId);
        data.put("style_text", result.getStyleText());
        data.put("last_summarized_chapter", result.getLastSummarizedChapter());
        data.put("version", result.getVersion());
        data.put("exists", result.isExists());

        return Result.success(data);
    }

    @PostMapping("/style-summary/refresh")
    public Result refreshStyleSummary(@RequestBody StyleSummaryRefreshDTO dto) {
        if (dto.getNovelId() == null) return Result.error("小说ID不能为空");

        String chapterSamples = dto.getChapterSamples() != null ? dto.getChapterSamples() : "";
        StyleSummaryResult result = knowledgeService.updateStyleSummary(dto.getNovelId(), chapterSamples);

        Map<String, Object> data = new HashMap<>();
        data.put("novel_id", dto.getNovelId());
        data.put("style_text", result.getStyleText());
        data.put("last_summarized_chapter", result.getLastSummarizedChapter());
        data.put("version", result.getVersion());
        data.put("exists", result.isExists());

        return Result.success(data);
    }

    @PutMapping("/style-summary")
    public Result updateStyleSummary(@RequestBody StyleSummaryUpdateDTO dto) {
        if (dto.getNovelId() == null) return Result.error("小说ID不能为空");
        if (dto.getStyleText() == null || dto.getStyleText().isBlank()) {
            return Result.error("风格文本不能为空");
        }

        // Get existing to preserve last_chapter
        StyleSummaryResult existing = knowledgeService.getStyleSummary(dto.getNovelId());
        int lastChapter = existing.isExists() ? existing.getLastSummarizedChapter() : 0;

        StyleSummaryResult result = knowledgeService.saveStyleSummary(
                dto.getNovelId(), dto.getStyleText(), lastChapter);

        Map<String, Object> data = new HashMap<>();
        data.put("novel_id", dto.getNovelId());
        data.put("style_text", result.getStyleText());
        data.put("last_summarized_chapter", result.getLastSummarizedChapter());
        data.put("version", result.getVersion());
        data.put("exists", result.isExists());

        return Result.success(data);
    }
}
