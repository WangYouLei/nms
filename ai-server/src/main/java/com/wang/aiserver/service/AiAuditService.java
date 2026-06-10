package com.wang.aiserver.service;

import com.wang.common.result.Result;
import com.wang.pojo.vo.AuditResultVO;

public interface AiAuditService {

    Result auditWithAi(String content, Long aimId, Integer aimType, AuditResultVO localResult);

    String callAiAudit(String content, String sensitiveWords);

    void saveAuditRecord(Long aimId, Integer aimType, String sensitiveWords, String aiResult, Integer level);
}
