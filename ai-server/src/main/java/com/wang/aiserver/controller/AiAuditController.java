package com.wang.aiserver.controller;

import com.wang.common.result.Result;
import com.wang.aiserver.service.AiAuditService;
import com.wang.pojo.dto.AiCommentAuditDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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


        log.info("AI审核请求：aimId={}, aimType={}, contentLength={}", request.getAimId(), request.getAimType(),
                request.getContent() != null ? request.getContent().length() : 0);

        if (request.getContent() == null || request.getContent().isEmpty()) {
            return Result.error("审核内容不能为空");
        }

        if (request.getAimType() == null) {
            return Result.error("审核对象类型不能为空");
        }

        return aiAuditService.auditWithAi(request.getContent(),request.getAimId(),request.getAimType(),request.getLocalResult());
    }
}
