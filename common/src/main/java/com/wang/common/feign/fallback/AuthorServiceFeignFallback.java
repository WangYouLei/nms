package com.wang.common.feign.fallback;

import com.wang.common.feign.AuthorServiceFeign;
import com.wang.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 作者服务 Feign 降级处理
 * 当 author-server 不可用时，返回默认值而非抛出异常，避免级联故障
 */
@Slf4j
@Component
public class AuthorServiceFeignFallback implements AuthorServiceFeign {

    @Override
    public Result getAuthorAvatar(Long authorId) {
        log.warn("[Feign降级] 获取作者头像失败，authorId={}", authorId);
        return Result.error("作者服务不可用");
    }

    @Override
    public Result batchGetAuthorAvatars(List<Long> authorIds) {
        log.warn("[Feign降级] 批量获取作者头像失败，authorIds={}", authorIds);
        return Result.error("作者服务不可用");
    }

    @Override
    public Result getAuthorBasicInfo(Long authorId) {
        log.warn("[Feign降级] 获取作者基本信息失败，authorId={}", authorId);
        return Result.error("作者服务不可用");
    }
}
