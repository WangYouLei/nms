package com.wang.commonserver.service;

import com.wang.common.result.Result;
import com.wang.pojo.vo.AuditResultVO;

/**
 * AI审核服务接口
 */
public interface AiAuditService {

    /**
     * AI审核文本内容
     * 逻辑：
     * 1. 本地敏感词校验
     * 2. 如果识别到敏感词且等级为1，调用AI审核并保存记录
     * 3. 如果等级为2，直接返回高危敏感词提示
     *
     * @param content    待审核内容
     * @param aimId    目标对象ID
     * @param aimType  目标对象类型
     * @return 审核结果
     */
    Result auditWithAi(String content, Long aimId, Integer aimType,AuditResultVO localResult);

    /**
     * 调用AI进行内容审核
     *
     * @param content        待审核内容
     * @param sensitiveWords 检测到的敏感词
     * @return AI审核意见
     */
    String callAiAudit(String content, String sensitiveWords);

    /**
     * 保存AI审核记录到manual_audit表
     *
     * @param aimId        目标对象ID
     * @param aimType      目标对象类型
     * @param sensitiveWords 检测到的敏感词
     * @param aiResult       AI审核意见
     * @param level          敏感等级
     */
    void saveAuditRecord(Long aimId, Integer aimType, String sensitiveWords, String aiResult, Integer level);
}