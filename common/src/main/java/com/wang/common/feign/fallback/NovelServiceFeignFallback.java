package com.wang.common.feign.fallback;

import com.wang.common.feign.NovelServiceFeign;
import com.wang.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 小说服务 Feign 降级处理
 * 当 novel-server 不可用时，返回默认值而非抛出异常，避免级联故障
 */
@Slf4j
@Component
public class NovelServiceFeignFallback implements NovelServiceFeign {

    @Override
    public Result getNovelAuthorId(Long novelId) {
        log.warn("[Feign降级] 获取小说作者ID失败，novelId={}", novelId);
        return Result.error("小说服务不可用");
    }

    @Override
    public Result batchGetNovelAuthorIds(List<Long> novelIds) {
        log.warn("[Feign降级] 批量获取小说作者ID失败，novelIds={}", novelIds);
        return Result.error("小说服务不可用");
    }

    @Override
    public Result getNovelBasicInfo(Long novelId) {
        log.warn("[Feign降级] 获取小说基本信息失败，novelId={}", novelId);
        return Result.error("小说服务不可用");
    }
}
