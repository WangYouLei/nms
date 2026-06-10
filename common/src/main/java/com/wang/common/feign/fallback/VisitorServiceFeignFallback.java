package com.wang.common.feign.fallback;

import com.wang.common.feign.VisitorServiceFeign;
import com.wang.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 访客服务 Feign 降级处理
 * 当 visitor-server 不可用时，返回默认值而非抛出异常，避免级联故障
 */
@Slf4j
@Component
public class VisitorServiceFeignFallback implements VisitorServiceFeign {

    @Override
    public Result getVisitorAvatar(Long visitorId) {
        log.warn("[Feign降级] 获取访客头像失败，visitorId={}", visitorId);
        return Result.error("访客服务不可用");
    }

    @Override
    public Result batchGetVisitorAvatars(List<Long> visitorIds) {
        log.warn("[Feign降级] 批量获取访客头像失败，visitorIds={}", visitorIds);
        return Result.error("访客服务不可用");
    }
}
