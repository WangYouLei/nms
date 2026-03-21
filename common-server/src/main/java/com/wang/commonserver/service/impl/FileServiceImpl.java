package com.wang.commonserver.service.impl;

import com.wang.common.config.DefaultUrlConfig;
import com.wang.common.enums.FileUploadTypeEnum;
import com.wang.common.result.Result;
import com.wang.commonserver.config.MinioInfo;
import com.wang.commonserver.service.FileService;
import io.minio.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * 文件存储服务实现类
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    private final MinioInfo minioInfo;
    private final MinioClient minioClient;
    private final DefaultUrlConfig defaultUrlConfig;

    private static final String CHAPTER_EXTENSION = ".md";
    private static final String CHAPTER_CONTENT_TYPE = "text/markdown";

    public FileServiceImpl(MinioInfo minioInfo, MinioClient minioClient, DefaultUrlConfig defaultUrlConfig) {
        this.minioInfo = minioInfo;
        this.minioClient = minioClient;
        this.defaultUrlConfig = defaultUrlConfig;
    }

    @Override
    public Result uploadFile(MultipartFile file, Integer code, Integer novelId, String oldFileUrl) {
        String typeName = FileUploadTypeEnum.getMessageByCode(code).name();
        if (!StringUtils.hasText(typeName)) {
            return Result.error("上传文件类型错误");
        }
        if (novelId != null) {
            typeName = typeName + "/" + novelId;
        }

        // 安全获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFileName = typeName + "/" + UUID.randomUUID() + extension;

        try {
            ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioInfo.getBucketName())
                    .object(newFileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build());

            if (response != null) {
                String url = minioInfo.getEndpoint() + "/" + minioInfo.getBucketName() + "/" + newFileName;
                deleteFile(oldFileUrl);
                log.info("文件上传成功: {}", url);
                return Result.success(url);
            }
        } catch (Exception e) {
            log.error("上传文件异常: {}", e.getMessage(), e);
        }
        log.error("上传文件失败");
        return Result.error("上传文件失败");
    }


    @Override
    public String getFileContent(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return null;
        }

        try {
            String objectName = extractObjectName(fileUrl);
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
            }
        } catch (Exception e) {
            log.error("获取文件内容失败: {}", fileUrl, e);
            return null;
        }
    }

    @Override
    public Result renameFile(String oldFileUrl, Integer code, Integer novelId, String newFileName) {
        if (!StringUtils.hasText(oldFileUrl)) {
            return Result.error("旧文件URL不能为空");
        }

        FileUploadTypeEnum typeEnum = FileUploadTypeEnum.getMessageByCode(code);
        if (typeEnum == null) {
            return Result.error("文件类型错误");
        }

        try {
            String oldObjectName = extractObjectName(oldFileUrl);
            String typeName = typeEnum.name();
            if (novelId != null) {
                typeName = typeName + "/" + novelId;
            }

            String safeFileName = sanitizeFileName(newFileName);
            String newObjectName = typeName + "/" + safeFileName + CHAPTER_EXTENSION;

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

            String newUrl = buildFileUrl(newObjectName);
            log.info("重命名文件成功: {} -> {}", oldFileUrl, newUrl);
            return Result.success(newUrl);
        } catch (Exception e) {
            log.error("重命名文件失败: {}", oldFileUrl, e);
            return Result.error("重命名文件失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            log.debug("文件URL为空，跳过删除");
            return false;
        }
        //判断这个文件地址是否是默认文件地址
        Map<Integer, String> novelType = defaultUrlConfig.getNovelType();
        if (novelType.containsValue(fileUrl)) {
            return true;
        }
        try {
            String objectName = extractObjectName(fileUrl);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioInfo.getBucketName())
                            .object(objectName)
                            .build()
            );
            log.info("文件删除成功: {}", objectName);
            return true;
        } catch (Exception e) {
            log.warn("删除文件失败: {}, 错误: {}", fileUrl, e.getMessage());
            return false;
        }
    }

    @Override
    public void downloadFile(String fileUrl, HttpServletResponse response) {
        if (!StringUtils.hasText(fileUrl)) {
            log.error("文件URL不能为空");
            return;
        }

        try {
            String objectName = extractObjectName(fileUrl);

            // 使用try-with-resources自动关闭资源
            try (InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioInfo.getBucketName())
                            .object(objectName)
                            .build()
            )) {
                // 获取文件名
                String fileName = objectName.substring(objectName.lastIndexOf("/") + 1);

                // 设置响应头
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment; filename=" +
                        URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));

                // 写入响应流
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, bytesRead);
                }

                response.getOutputStream().flush();
                log.info("文件下载成功: {}", objectName);
            }
        } catch (Exception e) {
            log.error("下载文件失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public String extractObjectName(String url) {
        String bucketName = minioInfo.getBucketName();
        int bucketIndex = url.indexOf(bucketName);
        if (bucketIndex != -1) {
            return url.substring(bucketIndex + bucketName.length() + 1);
        }
        // 尝试从路径中提取
        int slashIndex = url.indexOf("//");
        if (slashIndex != -1) {
            String path = url.substring(slashIndex + 2);
            int nextSlash = path.indexOf("/");
            if (nextSlash != -1) {
                return path.substring(nextSlash + 1);
            }
        }
        return url;
    }

    @Override
    public String buildFileUrl(String objectName) {
        return minioInfo.getEndpoint() + "/" + minioInfo.getBucketName() + "/" + objectName;
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