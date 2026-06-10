package com.wang.common.feign;

import com.wang.common.feign.fallback.NovelServiceFeignFallback;
import com.wang.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 小说服务 Feign 客户端
 * 供其他微服务调用 novel-server 的内部接口
 * 配置 fallback 降级处理，避免 novel-server 不可用时级联故障
 */
@FeignClient(name = "novel-server", fallback = NovelServiceFeignFallback.class)
public interface NovelServiceFeign {

    /**
     * 获取小说的作者ID
     * @param novelId 小说ID
     * @return 作者ID
     */
    @GetMapping("/internal/novel/authorId/{novelId}")
    Result getNovelAuthorId(@PathVariable("novelId") Long novelId);

    /**
     * 批量获取小说的作者ID
     * @param novelIds 小说ID列表
     * @return 小说ID与作者ID的映射
     */
    @PostMapping("/internal/novel/batch-author-ids")
    Result batchGetNovelAuthorIds(@RequestBody List<Long> novelIds);

    /**
     * 获取小说基本信息（用于收藏时填充冗余字段）
     * @param novelId 小说ID
     * @return 小说基本信息
     */
    @GetMapping("/internal/novel/basic/{novelId}")
    Result getNovelBasicInfo(@PathVariable("novelId") Long novelId);
}
