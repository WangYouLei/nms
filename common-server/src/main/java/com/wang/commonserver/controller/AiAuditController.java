package com.wang.commonserver.controller;

import com.wang.common.result.Result;
import com.wang.commonserver.service.AiAuditService;
import com.wang.pojo.dto.AiCommentAuditDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI审核控制器
 */
@Slf4j
@RestController
@Api(tags = "AI审核管理")
@RequestMapping("/aiAudit")
public class AiAuditController {

    private final AiAuditService aiAuditService;

    public AiAuditController(AiAuditService aiAuditService) {
        this.aiAuditService = aiAuditService;
    }

    @PostMapping("/audit")
    @ApiOperation("AI审核文本内容")
    public Result auditWithAi(
            @RequestBody @ApiParam("审核请求参数")AiCommentAuditDTO  request) {


        log.info("AI审核请求：aimId={}, aimType={}, content={}", request.getAimId(), request.getAimType(),request.getContent());

        if (request.getContent() == null || request.getContent().isEmpty()) {
            return Result.error("审核内容不能为空");
        }

        if (request.getAimId() == null || request.getAimType() == null) {
            return Result.error("审核对象ID和审核对象类型不能为空");
        }

        return aiAuditService.auditWithAi(request.getContent(),request.getAimId(),request.getAimType(),request.getLocalResult());
    }
}