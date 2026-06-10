package com.wang.common.feign;

import com.wang.common.feign.fallback.AiAuditServiceFeignFallback;
import com.wang.common.result.Result;
import com.wang.pojo.dto.AiCommentAuditDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ai-server", fallback = AiAuditServiceFeignFallback.class)
public interface AiAuditServiceFeign {

    @PostMapping("/aiAudit/audit")
    Result auditWithAi(@RequestBody AiCommentAuditDTO request);
}
