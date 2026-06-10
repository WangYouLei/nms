package com.wang.common.feign.fallback;

import com.wang.common.feign.ManagerServiceFeign;
import com.wang.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 管理员服务 Feign 降级处理
 * 当 manager-server 不可用时，返回默认值而非抛出异常，避免级联故障
 */
@Slf4j
@Component
public class ManagerServiceFeignFallback implements ManagerServiceFeign {

    @Override
    public Result getManagerAvatar(Long managerId) {
        log.warn("[Feign降级] 获取管理员头像失败，managerId={}", managerId);
        return Result.error("管理员服务不可用");
    }

    @Override
    public Result batchGetManagerAvatars(List<Long> managerIds) {
        log.warn("[Feign降级] 批量获取管理员头像失败，managerIds={}", managerIds);
        return Result.error("管理员服务不可用");
    }
}
