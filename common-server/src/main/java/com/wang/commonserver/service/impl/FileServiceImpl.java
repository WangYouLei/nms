package com.wang.commonserver.service.impl;

import com.wang.common.enums.FileUploadTypeEnum;
import com.wang.common.result.Result;
import com.wang.commonserver.config.MinioInfo;
import com.wang.commonserver.service.FileService;
import io.minio.*;
import io.minio.http.Method;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 文件存储服务实现类
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    private final MinioInfo minioInfo;
    private final MinioClient minioClient;

    public FileServiceImpl(MinioInfo minioInfo, MinioClient minioClient) {
        this.minioInfo = minioInfo;
        this.minioClient = minioClient;
    }

    @Override
    public Result uploadFile(MultipartFile file, Integer code,Integer novelId, String oldFileUrl) {
        String typeName = FileUploadTypeEnum.getMessageByCode(code).name();
        if (!StringUtils.hasText(typeName)) {
            return Result.error("上传文件类型错误");
        }
        if(novelId != null){
            typeName = typeName + "/" + novelId;
        }
        String newFileName = typeName + "/"
                + UUID.randomUUID().toString()
                + file.getOriginalFilename()
                .substring(file.getOriginalFilename().lastIndexOf("."));

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
    public boolean deleteFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            log.debug("文件URL为空，跳过删除");
            return false;
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
        
        InputStream inputStream = null;
        try {
            String objectName = extractObjectName(fileUrl);
            
            // 获取文件流
            inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioInfo.getBucketName())
                            .object(objectName)
                            .build()
            );
            
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
        } catch (Exception e) {
            log.error("下载文件失败: {}", e.getMessage(), e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    log.error("关闭输入流失败", e);
                }
            }
        }
    }


    /**
     * 从URL中提取对象名称
     * @param url
     * @return
     */
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


}