package com.wang.common.feign;

import com.wang.common.feign.fallback.ManagerServiceFeignFallback;
import com.wang.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 管理员服务 Feign 客户端
 * 供其他微服务调用 manager-server 的内部接口
 * 配置 fallback 降级处理，避免 manager-server 不可用时级联故障
 */
@FeignClient(name = "manager-server", fallback = ManagerServiceFeignFallback.class)
public interface ManagerServiceFeign {

    /**
     * 获取管理员头像
     * @param managerId 管理员ID
     * @return 头像URL
     */
    @GetMapping("/internal/manager/avatar/{managerId}")
    Result getManagerAvatar(@PathVariable("managerId") Long managerId);

    /**
     * 批量获取管理员头像
     * @param managerIds 管理员ID列表
     * @return 管理员ID与头像URL的映射
     */
    @PostMapping("/internal/manager/batch-avatars")
    Result batchGetManagerAvatars(@RequestBody List<Long> managerIds);
}
