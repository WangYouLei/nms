package com.wang.novel.feign;

import com.wang.common.result.Result;
import com.wang.pojo.vo.AuditResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "common-server")
public interface AiAuditServiceFeign {

    @PostMapping("/aiAudit/audit")
    Result auditWithAi(@RequestParam("content") String content,
                       @RequestParam("aimId") Long aimId,
                       @RequestParam("aimType") Integer aimType,
                       @RequestParam("localResult") AuditResultVO localResult);
}
