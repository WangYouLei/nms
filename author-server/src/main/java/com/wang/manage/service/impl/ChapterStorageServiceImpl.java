package com.wang.manage.service.impl;

import com.wang.manage.config.MinioInfo;
import com.wang.manage.service.ChapterStorageService;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 章节存储服务实现类
 * 封装所有 MinIO 文件操作
 */
@Slf4j
@Service
public class ChapterStorageServiceImpl implements ChapterStorageService {

    private static final String CHAPTER_PREFIX = "manager/NOVEL_CHAPTER/";
    private static final String FILE_EXTENSION = ".md";
    private static final String CONTENT_TYPE = "text/markdown";

    private final MinioClient minioClient;
    private final MinioInfo minioInfo;

    public ChapterStorageServiceImpl(MinioClient minioClient, MinioInfo minioInfo) {
        this.minioClient = minioClient;
        this.minioInfo = minioInfo;
    }

    @Override
    public String uploadChapterContent(Integer novelId, String title, String content) {
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream stream = new ByteArrayInputStream(contentBytes);
        return uploadChapterFile(novelId, title, stream, contentBytes.length);
    }

    @Override
    public String uploadChapterFile(Integer novelId, String title, InputStream stream, long size) {
        String objectName = buildObjectName(novelId, title);

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioInfo.getBucketName())
                            .object(objectName)
                            .stream(stream, size, -1)
                            .contentType(CONTENT_TYPE)
                            .build()
            );
            log.info("上传章节文件成功：{}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("上传章节文件失败：novelId={}, title={}", novelId, title, e);
            throw new RuntimeException("上传章节文件失败：" + e.getMessage(), e);
        }
    }

    @Override
    public String getChapterContent(String objectName) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioInfo.getBucketName())
                        .object(objectName)
                        .build()
        )) {
            StringBuilder content = new StringBuilder();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = stream.read(buffer)) != -1) {
                content.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
            }
            return content.toString();
        } catch (Exception e) {
            log.error("获取章节内容失败：objectName={}", objectName, e);
            throw new RuntimeException("获取章节内容失败：" + e.getMessage(), e);
        }
    }

    @Override
    public void deleteChapterContent(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioInfo.getBucketName())
                            .object(objectName)
                            .build()
            );
            log.info("删除章节文件成功：{}", objectName);
        } catch (Exception e) {
            log.warn("删除章节文件失败（继续执行）：objectName={}", objectName, e);
        }
    }

    @Override
    public String renameChapterContent(String oldObjectName, Integer novelId, String newTitle) {
        String newObjectName = buildObjectName(novelId, newTitle);

        try {
            // 复制到新路径
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(minioInfo.getBucketName())
                            .object(newObjectName)
                            .source(CopySource.builder()
                                    .bucket(minioInfo.getBucketName())
                                    .object(oldObjectName)
                                    .build())
                            .build()
            );

            // 删除旧文件
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioInfo.getBucketName())
                            .object(oldObjectName)
                            .build()
            );

            log.info("重命名章节文件成功：{} -> {}", oldObjectName, newObjectName);
            return newObjectName;
        } catch (Exception e) {
            log.error("重命名章节文件失败：old={}, new={}", oldObjectName, newObjectName, e);
            throw new RuntimeException("重命名章节文件失败：" + e.getMessage(), e);
        }
    }

    @Override
    public String extractObjectName(String contentUrl) {
        String bucketName = minioInfo.getBucketName();
        int bucketIndex = contentUrl.indexOf(bucketName);
        if (bucketIndex != -1) {
            return contentUrl.substring(bucketIndex + bucketName.length() + 1);
        }
        int managerIndex = contentUrl.indexOf("manager/");
        if (managerIndex != -1) {
            return contentUrl.substring(managerIndex);
        }
        return contentUrl;
    }

    @Override
    public String buildContentUrl(String objectName) {
        return minioInfo.getEndpoint() + "/" + minioInfo.getBucketName() + "/" + objectName;
    }

    /**
     * 构建存储路径
     */
    private String buildObjectName(Integer novelId, String title) {
        String fileName = sanitizeFileName(title) + FILE_EXTENSION;
        return CHAPTER_PREFIX + novelId + "/" + fileName;
    }

    /**
     * 清理文件名中的非法字符
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "untitled";
        }
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_")
                .trim();
    }
}