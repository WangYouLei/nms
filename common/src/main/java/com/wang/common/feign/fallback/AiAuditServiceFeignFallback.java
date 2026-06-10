package com.wang.common.feign.fallback;

import com.wang.common.feign.AiAuditServiceFeign;
import com.wang.common.result.Result;
import com.wang.pojo.dto.AiCommentAuditDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AiAuditServiceFeignFallback implements AiAuditServiceFeign {

    @Override
    public Result auditWithAi(AiCommentAuditDTO request) {
        log.warn("[Feign降级] AI审核服务不可用：aimId={}, aimType={}", request.getAimId(), request.getAimType());
        return Result.error("AI审核服务不可用");
    }
}
