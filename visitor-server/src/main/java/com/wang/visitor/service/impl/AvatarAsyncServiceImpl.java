package com.wang.visitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wang.common.enums.FileUploadTypeEnum;
import com.wang.pojo.entity.Visitor;
import com.wang.visitor.mapper.VisitorMapper;
import com.wang.visitor.service.AvatarAsyncService;
import com.wang.visitor.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 头像异步上传服务实现类
 */
@Slf4j
@Service
public class AvatarAsyncServiceImpl implements AvatarAsyncService {

    private final CommonService commonService;

    private final VisitorMapper visitorMapper;
    public AvatarAsyncServiceImpl(CommonService commonService, VisitorMapper visitorMapper) {
        this.commonService = commonService;
        this.visitorMapper = visitorMapper;
    }

    /**
     * 异步上传头像到MinIO
     * 使用@Async注解，Spring会在线程池中异步执行此方法
     */
    @Async
    @Override
    public void uploadAvatarAsync(MultipartFile file, Integer id) {
        log.info("异步上传头像开始，线程：{}", Thread.currentThread().getName());
        try {
            String avatarUrl = commonService.fileUpload(file, FileUploadTypeEnum.USER_AVATAR.getCode());

            if (avatarUrl != null) {
                int updateCount = visitorMapper.update(
                        Wrappers.<Visitor>lambdaUpdate()
                                .set(Visitor::getAvatar, avatarUrl)
                                .eq(Visitor::getId, id)
                );

                if (updateCount > 0) {
                    log.info("头像更新成功，用户 ID: {}, avatarUrl: {}", id, avatarUrl);
                } else {
                    log.warn("头像更新失败，用户 ID: {} 可能不存在", id);
                }
            } else {
                log.error("文件上传失败，返回 URL 为空");
            }

        } catch (Exception e) {
            log.error("异步上传头像异常：{}", e.getMessage(), e);
        }
    }

}