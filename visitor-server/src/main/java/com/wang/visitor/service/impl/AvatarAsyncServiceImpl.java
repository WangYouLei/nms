package com.wang.visitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wang.common.enums.FileUploadTypeEnum;
import com.wang.pojo.entity.Visitor;
import com.wang.visitor.config.MinioInfo;
import com.wang.visitor.mapper.VisitorMapper;
import com.wang.visitor.service.AvatarAsyncService;
import com.wang.visitor.service.CommonService;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 头像异步上传服务实现类
 */
@Slf4j
@Service
public class AvatarAsyncServiceImpl implements AvatarAsyncService {

    private final CommonService commonService;
    private final VisitorMapper visitorMapper;
    private final MinioClient minioClient;
    private final MinioInfo minioInfo;

    public AvatarAsyncServiceImpl(CommonService commonService, 
                                   VisitorMapper visitorMapper,
                                   MinioClient minioClient,
                                   MinioInfo minioInfo) {
        this.commonService = commonService;
        this.visitorMapper = visitorMapper;
        this.minioClient = minioClient;
        this.minioInfo = minioInfo;
    }

    /**
     * 异步上传头像到MinIO
     * 使用@Async注解，Spring会在线程池中异步执行此方法
     * 上传成功后会删除旧头像文件
     */
    @Async
    @Override
    public void uploadAvatarAsync(MultipartFile file, Integer id) {
        log.info("异步上传头像开始，线程：{}", Thread.currentThread().getName());
        
        // 1. 先查询用户获取旧头像URL
        Visitor visitor = visitorMapper.selectById(id);
        if (visitor == null) {
            log.warn("用户不存在，ID: {}", id);
            return;
        }
        String oldAvatarUrl = visitor.getAvatar();
        
        try {
            // 2. 上传新头像
            String newAvatarUrl = commonService.fileUpload(file, FileUploadTypeEnum.USER_AVATAR.getCode());

            if (newAvatarUrl != null) {
                // 3. 更新数据库
                int updateCount = visitorMapper.update(
                        Wrappers.<Visitor>lambdaUpdate()
                                .set(Visitor::getAvatar, newAvatarUrl)
                                .eq(Visitor::getId, id)
                );

                if (updateCount > 0) {
                    log.info("头像更新成功，用户ID: {}, 新头像URL: {}", id, newAvatarUrl);
                    
                    // 4. 删除旧头像（在数据库更新成功后）
                    deleteOldAvatar(oldAvatarUrl);
                } else {
                    log.warn("头像更新失败，用户ID: {} 可能不存在", id);
                }
            } else {
                log.error("文件上传失败，返回URL为空");
            }

        } catch (Exception e) {
            log.error("异步上传头像异常：{}", e.getMessage(), e);
        }
    }
    
    /**
     * 删除旧头像文件
     * @param oldAvatarUrl 旧头像的完整URL
     */
    private void deleteOldAvatar(String oldAvatarUrl) {
        if (!StringUtils.hasText(oldAvatarUrl)) {
            log.debug("旧头像URL为空，跳过删除");
            return;
        }
        
        // 检查是否是本系统的头像（visitor/USER_AVATAR/ 开头）
        if (!oldAvatarUrl.contains("visitor/USER_AVATAR/")) {
            log.debug("旧头像不是本系统上传的，跳过删除: {}", oldAvatarUrl);
            return;
        }
        
        try {
            // 从URL提取objectName
            String objectName = extractObjectName(oldAvatarUrl);
            
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioInfo.getBucketName())
                            .object(objectName)
                            .build()
            );
            log.info("旧头像删除成功: {}", objectName);
        } catch (Exception e) {
            // 删除失败不影响主流程，只记录日志
            log.warn("删除旧头像失败: {}, 错误: {}", oldAvatarUrl, e.getMessage());
        }
    }
    
    /**
     * 从完整URL中提取MinIO对象名称
     */
    private String extractObjectName(String url) {
        String bucketName = minioInfo.getBucketName();
        int bucketIndex = url.indexOf(bucketName);
        if (bucketIndex != -1) {
            return url.substring(bucketIndex + bucketName.length() + 1);
        }
        // 如果找不到bucket，尝试找visitor/
        int visitorIndex = url.indexOf("visitor/");
        if (visitorIndex != -1) {
            return url.substring(visitorIndex);
        }
        return url;
    }
}