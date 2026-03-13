package com.wang.visitor.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

/**
 * 头像异步上传服务接口
 */
public interface AvatarAsyncService {

    /**
     * 异步上传头像到MinIO
     * @param file 头像文件
     * @return CompletableFuture包含头像URL，失败时返回null
     */
     void uploadAvatarAsync(MultipartFile file,Integer id);
}