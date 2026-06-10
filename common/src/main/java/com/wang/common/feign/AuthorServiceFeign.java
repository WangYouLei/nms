package com.wang.common.feign;

import com.wang.common.feign.fallback.AuthorServiceFeignFallback;
import com.wang.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 作者服务 Feign 客户端
 * 供其他微服务调用 author-server 的内部接口
 * 配置 fallback 降级处理，避免 author-server 不可用时级联故障
 */
@FeignClient(name = "author-server", fallback = AuthorServiceFeignFallback.class)
public interface AuthorServiceFeign {

    /**
     * 获取作者头像
     * @param authorId 作者ID
     * @return 头像URL
     */
    @GetMapping("/internal/author/avatar/{authorId}")
    Result getAuthorAvatar(@PathVariable("authorId") Long authorId);

    /**
     * 批量获取作者头像
     * @param authorIds 作者ID列表
     * @return 作者ID与头像URL的映射
     */
    @PostMapping("/internal/author/batch-avatars")
    Result batchGetAuthorAvatars(@RequestBody List<Long> authorIds);

    /**
     * 获取作者基本信息（名称、头像、等级、简介、作品数）
     * @param authorId 作者ID
     * @return 作者基本信息
     */
    @GetMapping("/internal/author/basic/{authorId}")
    Result getAuthorBasicInfo(@PathVariable("authorId") Long authorId);
}
