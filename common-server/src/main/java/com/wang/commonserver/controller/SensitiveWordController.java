package com.wang.commonserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.result.Result;
import com.wang.commonserver.service.SensitiveWordService;
import com.wang.pojo.dto.SensitiveWordDTO;
import com.wang.pojo.dto.SensitiveWordQueryDTO;
import com.wang.pojo.vo.AuditResultVO;
import com.wang.pojo.vo.SensitiveWordVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * 敏感词控制器
 */
@Slf4j
@RestController
@Api(tags = "敏感词管理")
@RequestMapping("/sensitive-word")
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    public SensitiveWordController(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    @PostMapping("/add")
    @ApiOperation("添加敏感词")
    public Result addSensitiveWord(@RequestBody SensitiveWordDTO dto) {
        log.info("添加敏感词请求：word={}", dto.getWord());
        return sensitiveWordService.addSensitiveWord(dto);
    }

    @PostMapping("/batch-add")
    @ApiOperation("批量添加敏感词")
    public Result batchAddSensitiveWord(
            @RequestBody @ApiParam("敏感词列表") Set<String> words,
            @RequestParam(required = false) @ApiParam("类别：1-涉政，2-涉黄，3-涉暴，4-广告，5-其他") Integer category,
            @RequestParam(required = false) @ApiParam("等级：1-低，2-高") Integer level,
            @RequestParam(required = false) @ApiParam("创建人ID") Long creatorId) {
        log.info("批量添加敏感词请求：count={}", words.size());
        return sensitiveWordService.batchAddSensitiveWord(words, category, level, creatorId);
    }

    @PutMapping("/update")
    @ApiOperation("更新敏感词")
    public Result updateSensitiveWord(@RequestBody SensitiveWordDTO dto) {
        log.info("更新敏感词请求：id={}", dto.getId());
        return sensitiveWordService.updateSensitiveWord(dto);
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除敏感词")
    public Result deleteSensitiveWord(@PathVariable @ApiParam("敏感词ID") Long id) {
        log.info("删除敏感词请求：id={}", id);
        return sensitiveWordService.deleteSensitiveWord(id);
    }

    @DeleteMapping("/batch-delete")
    @ApiOperation("批量删除敏感词")
    public Result batchDeleteSensitiveWord(@RequestBody @ApiParam("敏感词ID列表") Set<Long> ids) {
        log.info("批量删除敏感词请求：count={}", ids.size());
        return sensitiveWordService.batchDeleteSensitiveWord(ids);
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("获取敏感词详情")
    public Result getSensitiveWordById(@PathVariable @ApiParam("敏感词ID") Long id) {
        log.info("获取敏感词详情请求：id={}", id);
        return sensitiveWordService.getSensitiveWordById(id);
    }

    @PostMapping("/list")
    @ApiOperation("分页查询敏感词列表")
    public Result getSensitiveWordList(@RequestBody SensitiveWordQueryDTO queryDTO) {
        log.info("分页查询敏感词列表请求");
        return sensitiveWordService.getSensitiveWordList(queryDTO);
    }

    @PutMapping("/status/{id}")
    @ApiOperation("启用/禁用敏感词")
    public Result updateStatus(
            @PathVariable @ApiParam("敏感词ID") Long id,
            @RequestParam @ApiParam("状态：0-禁用，1-启用") Integer status) {
        log.info("更新敏感词状态请求：id={}, status={}", id, status);
        return sensitiveWordService.updateStatus(id, status);
    }


    @PostMapping("/detect")
    @ApiOperation("检测文本中的敏感词")
    public Result detectSensitiveWords(@RequestBody @ApiParam("待检测内容") Map<String, String> request) {
        String content = request.get("content");
        log.info("检测敏感词请求：length={}", content != null ? content.length() : 0);
        Set<String> words = sensitiveWordService.detectSensitiveWords(content);
        return Result.success(words);
    }

    @PostMapping("/filter")
    @ApiOperation("过滤文本中的敏感词")
    public Result filterText(
            @RequestBody @ApiParam("待过滤内容") Map<String, String> request) {
        String content = request.get("content");
        log.info("过滤敏感词请求：length={}", content != null ? content.length() : 0);
        Character replacement = '*';
        String filtered = sensitiveWordService.filterText(content, replacement);
        return Result.success(filtered);
    }

    @PostMapping("/refresh-cache")
    @ApiOperation("刷新敏感词缓存")
    public Result refreshCache() {
        log.info("刷新敏感词缓存请求");
        sensitiveWordService.refreshCache();
        return Result.success("缓存刷新成功");
    }

    @PostMapping("/auditText")
    @ApiOperation("本地敏感词审核")
    public Result auditText(
            @RequestBody @ApiParam("待审核内容") Map<String, String> request) {
        String content = request.get("content");
        log.info("本地敏感词审核请求：length={}", content != null ? content.length() : 0);
        AuditResultVO result = sensitiveWordService.auditText(content, true);
        return Result.success(result);
    }
}