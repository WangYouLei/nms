package com.wang.aiserver.controller;

import com.wang.aiserver.service.KnowledgeService;
import com.wang.common.result.Result;
import com.wang.pojo.dto.KnowledgeItemDTO;
import com.wang.pojo.entity.KnowledgeItem;
import com.wang.pojo.vo.KnowledgeItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @GetMapping("/items")
    public Result listItems(@RequestParam Long novelId,
                                                    @RequestParam(required = false) String itemType,
                                                    @RequestParam(defaultValue = "0") int minStatus) {
        List<KnowledgeItem> items = knowledgeService.getAllForNovel(novelId, itemType, minStatus);
        List<KnowledgeItemVO> vos = new ArrayList<>();
        for (KnowledgeItem item : items) {
            vos.add(toVO(item));
        }
        return Result.success(vos);
    }

    @GetMapping("/item/{id}")
    public Result getItem(@PathVariable Long id) {
        return Result.error("暂不支持单独查询，请使用列表接口");
    }

    @PostMapping("/item")
    public Result createItem(@RequestBody KnowledgeItemDTO dto) {
        if (dto.getNovelId() == null) return Result.error("小说ID不能为空");
        if (dto.getName() == null || dto.getName().isBlank()) return Result.error("名称不能为空");

        KnowledgeItem item = new KnowledgeItem();
        item.setNovelId(dto.getNovelId());
        item.setItemType(dto.getItemType());
        item.setName(dto.getName());
        item.setContent(dto.getContent());
        item.setSummary(dto.getSummary());
        item.setSourceChapterId(dto.getSourceChapterId());
        item.setSourceChapterOrder(dto.getSourceChapterOrder());
        item.setConfidence(dto.getConfidence());
        item.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        KnowledgeItem saved = knowledgeService.saveItem(item);
        return Result.success(toVO(saved));
    }

    @PutMapping("/item/{id}")
    public Result updateItem(@PathVariable Long id, @RequestBody KnowledgeItemDTO dto) {
        KnowledgeItem item = new KnowledgeItem();
        item.setId(id);
        item.setNovelId(dto.getNovelId());
        item.setItemType(dto.getItemType());
        item.setName(dto.getName());
        item.setContent(dto.getContent());
        item.setSummary(dto.getSummary());
        item.setConfidence(dto.getConfidence());
        item.setStatus(dto.getStatus());
        KnowledgeItem saved = knowledgeService.saveItem(item);
        return Result.success(toVO(saved));
    }

    @PatchMapping("/item/{id}/status")
    public Result updateItemStatus(@PathVariable Long id, @RequestBody KnowledgeItemDTO dto) {
        if (dto.getStatus() == null) return Result.error("状态不能为空");

        KnowledgeItem saved = knowledgeService.updateItemStatus(id, dto.getStatus());
        if (saved == null) return Result.error("知识项不存在");
        return Result.success(toVO(saved));
    }

    @DeleteMapping("/item/{id}")
    public Result deleteItem(@PathVariable Long id) {
        knowledgeService.deleteItem(id);
        return Result.success();
    }

    private KnowledgeItemVO toVO(KnowledgeItem item) {
        KnowledgeItemVO vo = new KnowledgeItemVO();
        vo.setId(item.getId());
        vo.setNovelId(item.getNovelId());
        vo.setItemType(item.getItemType());
        vo.setName(item.getName());
        vo.setContent(item.getContent());
        vo.setSummary(item.getSummary());
        vo.setSourceChapterId(item.getSourceChapterId());
        vo.setSourceChapterOrder(item.getSourceChapterOrder());
        vo.setConfidence(item.getConfidence());
        vo.setVersion(item.getVersion());
        vo.setStatus(item.getStatus());
        vo.setCreatedAt(item.getCreatedAt());
        vo.setUpdatedAt(item.getUpdatedAt());
        return vo;
    }
}
