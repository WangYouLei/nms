package com.wang.manage.controller;

import com.wang.common.result.Result;
import com.wang.manage.service.AuthorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 作者内部接口控制器
 * 供其他微服务通过 Feign 调用，不对外暴露给前端
 * 路径前缀 /internal 用于区分内部调用和外部请求
 */
@Slf4j
@RestController
@RequestMapping("/internal/author")
public class AuthorInternalController {

    private final AuthorService authorService;

    public AuthorInternalController(AuthorService authorService) {
        this.authorService = authorService;
    }

    /**
     * 获取作者头像URL
     * @param authorId 作者ID
     */
    @GetMapping("/avatar/{authorId}")
    public Result getAuthorAvatar(@PathVariable Long authorId) {
        log.info("[内部调用] 获取作者头像：authorId={}", authorId);
        return authorService.getAuthorAvatar(authorId);
    }

    /**
     * 批量获取作者头像URL
     * @param authorIds 作者ID列表
     */
    @PostMapping("/batch-avatars")
    public Result batchGetAuthorAvatars(@RequestBody List<Long> authorIds) {
        log.info("[内部调用] 批量获取作者头像：count={}", authorIds.size());
        return authorService.batchGetAuthorAvatars(authorIds);
    }

    /**
     * 获取作者基本信息（名称、头像、等级、简介、作品数）
     * @param authorId 作者ID
     */
    @GetMapping("/basic/{authorId}")
    public Result getAuthorBasicInfo(@PathVariable Long authorId) {
        log.info("[内部调用] 获取作者基本信息：authorId={}", authorId);
        return authorService.getAuthorBasicInfo(authorId);
    }
}
