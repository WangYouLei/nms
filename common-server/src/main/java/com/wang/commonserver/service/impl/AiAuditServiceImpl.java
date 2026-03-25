package com.wang.commonserver.service.impl;

import com.wang.common.result.Result;
import com.wang.commonserver.mapper.ManualAuditMapper;
import com.wang.commonserver.service.AiAuditService;
import com.wang.commonserver.service.SensitiveWordService;
import com.wang.pojo.entity.ManualAudit;
import com.wang.pojo.vo.AuditResultVO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI审核服务实现类
 */
@Slf4j
@Service
public class AiAuditServiceImpl implements AiAuditService {

    private final SensitiveWordService sensitiveWordService;
    private final ManualAuditMapper manualAuditMapper;
    private final ChatClient textClient1;

    /**
     * AI审核提示词模板
     */
    private String promptTemplate;

    public AiAuditServiceImpl(
            SensitiveWordService sensitiveWordService,
            ManualAuditMapper manualAuditMapper,
            @Qualifier("textClient1") ChatClient textClient1) {
        this.sensitiveWordService = sensitiveWordService;
        this.manualAuditMapper = manualAuditMapper;
        this.textClient1 = textClient1;
    }

    /**
     * 启动时加载提示词模板
     */
    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/ai-audit-prompt.txt");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                this.promptTemplate = reader.lines().collect(Collectors.joining("\n"));
            }
            log.info("AI审核提示词模板加载成功");
        } catch (Exception e) {
            log.warn("加载AI审核提示词模板失败，使用默认模板", e);
            this.promptTemplate = getDefaultPromptTemplate();
        }
    }

    @Override
    public Result auditWithAi(String content, Long aimId, Integer aimType,AuditResultVO localResult) {
        log.info("开始AI审核：aimId={}, aimType={}, contentLength={}", aimId, aimType, content != null ? content.length() : 0);

        if (content == null || content.isEmpty()) {
            return Result.success(AuditResultVO.passed());
        }

        // 未检测到敏感词，直接通过
        if (localResult.getPassed() && localResult.getResult() == 1) {
            log.info("内容审核通过，无敏感词");
            return Result.success();
        }

        Set<String> sensitiveWords = localResult.getSensitiveWords();
        String wordsStr = String.join("、", sensitiveWords);

        // 2. 检查敏感等级
        Integer maxLevel = localResult.getMaxLevel();

        if (maxLevel == 2) {
            // 高危敏感词，直接拒绝
            log.warn("检测到高危敏感词：{}", wordsStr);
            return Result.error("检测到高危敏感词！" + wordsStr);
        }

        // 3. 等级为1，调用AI审核
        log.info("检测到低级敏感词，调用AI审核：{}", wordsStr);
        String aiResult = callAiAudit(content, wordsStr);

        // 4. 保存审核记录
        saveAuditRecord(aimId, aimType, wordsStr, aiResult, maxLevel);

        // 5. 返回审核结果
        localResult.setResultDesc("AI审核完成，需人工复核");
        return Result.success(java.util.Map.of(
                "auditResult", localResult,
                "aiResult", aiResult
        ));
    }

    @Override
    public String callAiAudit(String content, String sensitiveWords) {
        log.info("调用AI审核敏感词：{}", sensitiveWords);

        try {
            String prompt = buildAiPrompt(content, sensitiveWords);

            String aiResponse = textClient1.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("AI审核完成，结果：{}", aiResponse);
            return aiResponse != null ? aiResponse : "AI审核无响应";

        } catch (Exception e) {
            log.error("AI审核调用失败", e);
            return "AI审核服务异常，请人工审核";
        }
    }

    @Override
    @Transactional
    public void saveAuditRecord(Long aimId, Integer aimType, String sensitiveWords, String aiResult, Integer level) {
        log.info("保存AI审核记录：aimId={}, aimType={}", aimId, aimType);

        ManualAudit record = new ManualAudit();
        record.setAimId(aimId);
        record.setAimType(aimType);
        record.setResult(0);
        record.setAiResult(String.format("敏感词：%s\nAI意见：%s", sensitiveWords, aiResult));
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        manualAuditMapper.insert(record);
        log.info("AI审核记录保存成功，recordId={}", record.getId());
    }

    /**
     * 构建AI审核提示词
     */
    private String buildAiPrompt(String content, String sensitiveWords) {
        // 截取内容，避免过长
        String truncatedContent = content.length() > 500 ? content.substring(0, 500) + "..." : content;
        
        return promptTemplate
                .replace("${sensitiveWords}", sensitiveWords)
                .replace("${content}", truncatedContent);
    }

    /**
     * 默认提示词模板（加载失败时使用）
     */
    private String getDefaultPromptTemplate() {
        return "你是一个内容审核专家。请审核以下内容中的敏感词是否真的违规。\n\n" +
                "检测到的敏感词：${sensitiveWords}\n\n" +
                "待审核内容：${content}\n\n" +
                "请简要分析这些敏感词在当前语境下是否违规，给出审核意见（不超过100字）。格式：是否违规：是/否。理由：...";
    }
}