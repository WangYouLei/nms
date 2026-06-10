package com.wang.visitor.controller;

import com.wang.common.result.Result;
import com.wang.visitor.service.VisitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 访客内部接口控制器
 * 供其他微服务通过 Feign 调用，不对外暴露给前端
 */
@Slf4j
@RestController
@RequestMapping("/internal/visitor")
public class VisitorInternalController {

    private final VisitorService visitorService;

    public VisitorInternalController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    /**
     * 获取访客头像URL
     * @param visitorId 访客ID
     */
    @GetMapping("/avatar/{visitorId}")
    public Result getVisitorAvatar(@PathVariable Long visitorId) {
        log.info("[内部调用] 获取访客头像：visitorId={}", visitorId);
        return visitorService.getVisitorAvatar(visitorId);
    }

    /**
     * 批量获取访客头像URL
     * @param visitorIds 访客ID列表
     */
    @PostMapping("/batch-avatars")
    public Result batchGetVisitorAvatars(@RequestBody List<Long> visitorIds) {
        log.info("[内部调用] 批量获取访客头像：count={}", visitorIds.size());
        return visitorService.batchGetVisitorAvatars(visitorIds);
    }
}
