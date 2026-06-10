package com.wang.common.feign;

import com.wang.common.feign.fallback.VisitorServiceFeignFallback;
import com.wang.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 访客服务 Feign 客户端
 * 供其他微服务调用 visitor-server 的内部接口
 * 配置 fallback 降级处理，避免 visitor-server 不可用时级联故障
 */
@FeignClient(name = "visitor-server", fallback = VisitorServiceFeignFallback.class)
public interface VisitorServiceFeign {

    /**
     * 获取访客头像
     * @param visitorId 访客ID
     * @return 头像URL
     */
    @GetMapping("/internal/visitor/avatar/{visitorId}")
    Result getVisitorAvatar(@PathVariable("visitorId") Long visitorId);

    /**
     * 批量获取访客头像
     * @param visitorIds 访客ID列表
     * @return 访客ID与头像URL的映射
     */
    @PostMapping("/internal/visitor/batch-avatars")
    Result batchGetVisitorAvatars(@RequestBody List<Long> visitorIds);
}
