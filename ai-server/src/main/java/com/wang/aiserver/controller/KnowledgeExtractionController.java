package com.wang.aiserver.controller;

import com.wang.aiserver.service.KnowledgeExtractResult;
import com.wang.aiserver.service.KnowledgeService;
import com.wang.common.result.Result;
import com.wang.pojo.dto.KnowledgeExtractDTO;
import com.wang.pojo.vo.KnowledgeExtractVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/knowledge")
public class KnowledgeExtractionController {

    private final KnowledgeService knowledgeService;

    public KnowledgeExtractionController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @PostMapping("/extract")
    public Result extract(@RequestBody KnowledgeExtractDTO dto) {
        log.info("知识提取请求：novelId={}, chapterId={}, order={}",
                dto.getNovelId(), dto.getChapterId(), dto.getChapterOrder());

        if (dto.getNovelId() == null) return Result.error("小说ID不能为空");
        if (dto.getChapterText() == null || dto.getChapterText().isBlank()) {
            return Result.error("章节文本不能为空");
        }

        KnowledgeExtractResult result = knowledgeService.extractAndStore(
                dto.getNovelId(),
                dto.getChapterId(),
                dto.getChapterOrder() != null ? dto.getChapterOrder() : 1,
                dto.getChapterText(),
                dto.getNovelInfo());

        KnowledgeExtractVO vo = new KnowledgeExtractVO();
        vo.setExtractedCount(result.getExtractedCount());
        vo.setNewItems(convertItems(result.getNewItems()));
        vo.setUpdatedItems(convertItems(result.getUpdatedItems()));

        return Result.success(vo);
    }

    @PostMapping("/extract-batch")
    public Result extractBatch(@RequestBody Map<String, Object> body) {
        Long novelId = toLong(body.get("novelId"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> chapters = (List<Map<String, Object>>) body.get("chapters");

        if (novelId == null) return Result.error("小说ID不能为空");
        if (chapters == null || chapters.isEmpty()) return Result.error("章节列表不能为空");

        int totalExtracted = 0;
        int totalNew = 0;
        int totalUpdated = 0;
        List<Map<String, Object>> chapterResults = new ArrayList<>();

        for (Map<String, Object> chapter : chapters) {
            Long chapterId = toLong(chapter.get("chapter_id"));
            int chapterOrder = chapter.get("chapter_order") instanceof Number n
                    ? n.intValue() : 0;
            String chapterText = (String) chapter.getOrDefault("text", "");
            String novelInfo = (String) chapter.getOrDefault("novel_info", "");

            if (chapterText.isBlank()) continue;

            KnowledgeExtractResult result = knowledgeService.extractAndStore(
                    novelId, chapterId != null ? chapterId : 0L,
                    chapterOrder, chapterText, novelInfo);

            totalExtracted += result.getExtractedCount();
            totalNew += result.getNewItems().size();
            totalUpdated += result.getUpdatedItems().size();

            Map<String, Object> chResult = Map.of(
                    "chapter_id", chapterId != null ? chapterId : 0,
                    "extracted_count", result.getExtractedCount(),
                    "new_count", result.getNewItems().size(),
                    "updated_count", result.getUpdatedItems().size()
            );
            chapterResults.add(chResult);
        }

        log.info("批量知识提取完成: novelId={}, totalExtracted={}, new={}, updated={}",
                novelId, totalExtracted, totalNew, totalUpdated);

        return Result.success(Map.of(
                "total_extracted", totalExtracted,
                "total_new", totalNew,
                "total_updated", totalUpdated,
                "chapter_results", chapterResults
        ));
    }

    @SuppressWarnings("unchecked")
    private List<KnowledgeExtractVO.ItemSummary> convertItems(List<Object> items) {
        List<KnowledgeExtractVO.ItemSummary> result = new ArrayList<>();
        for (Object obj : items) {
            if (obj instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) obj;
                KnowledgeExtractVO.ItemSummary summary = new KnowledgeExtractVO.ItemSummary();
                summary.setId(toLong(map.get("id")));
                summary.setItemType((String) map.get("itemType"));
                summary.setName((String) map.get("name"));
                summary.setSummary((String) map.get("summary"));
                result.add(summary);
            }
        }
        return result;
    }

    private Long toLong(Object obj) {
        if (obj instanceof Number) return ((Number) obj).longValue();
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
