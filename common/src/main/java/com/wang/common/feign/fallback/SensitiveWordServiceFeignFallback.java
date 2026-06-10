package com.wang.common.feign.fallback;

import com.wang.common.feign.SensitiveWordServiceFeign;
import com.wang.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class SensitiveWordServiceFeignFallback implements SensitiveWordServiceFeign {

    @Override
    public Result detectSensitiveWords(Map<String, String> request) {
        log.warn("[Feign降级] 敏感词检测失败");
        return Result.error("敏感词服务不可用");
    }

    @Override
    public Result filterText(Map<String, String> request) {
        log.warn("[Feign降级] 敏感词过滤失败");
        return Result.error("敏感词服务不可用");
    }

    @Override
    public Result auditText(Map<String, String> request) {
        log.warn("[Feign降级] 敏感词审核失败");
        return Result.error("敏感词服务不可用");
    }
}
