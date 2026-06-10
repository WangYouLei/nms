package com.wang.manager.controller;

import com.wang.common.result.Result;
import com.wang.manager.service.ManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员内部接口控制器
 * 供其他微服务通过 Feign 调用，不对外暴露给前端
 */
@Slf4j
@RestController
@RequestMapping("/internal/manager")
public class ManagerInternalController {

    private final ManagerService managerService;

    public ManagerInternalController(ManagerService managerService) {
        this.managerService = managerService;
    }

    /**
     * 获取管理员头像URL
     * @param managerId 管理员ID
     */
    @GetMapping("/avatar/{managerId}")
    public Result getManagerAvatar(@PathVariable Long managerId) {
        log.info("[内部调用] 获取管理员头像：managerId={}", managerId);
        return managerService.getManagerAvatar(managerId);
    }

    /**
     * 批量获取管理员头像URL
     * @param managerIds 管理员ID列表
     */
    @PostMapping("/batch-avatars")
    public Result batchGetManagerAvatars(@RequestBody List<Long> managerIds) {
        log.info("[内部调用] 批量获取管理员头像：count={}", managerIds.size());
        return managerService.batchGetManagerAvatars(managerIds);
    }
}
