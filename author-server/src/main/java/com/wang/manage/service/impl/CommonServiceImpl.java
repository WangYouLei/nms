package com.wang.manage.service.impl;

import com.wang.common.enums.FileUploadTypeEnum;
import com.wang.manage.config.MinioInfo;
import com.wang.manage.service.CommonService;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
public class CommonServiceImpl implements CommonService {

    private final MinioInfo minioInfo;
    private final MinioClient minioClient;

    public CommonServiceImpl(MinioInfo minioInfo, MinioClient minioClient) {
        this.minioInfo = minioInfo;
        this.minioClient = minioClient;
    }

    /**
     * 文件上传
     *
     * @param file 文件
     * @param code 文件类型编码
     * @return 文件访问URL
     */
    @Override
    public String fileUpload(MultipartFile file, Integer code) {
        String typeName = FileUploadTypeEnum.getMessageByCode(code).name();
        String newFileName = "manager/" + typeName + "/"
                + UUID.randomUUID().toString()
                + file.getOriginalFilename()
                .substring(file.getOriginalFilename().lastIndexOf("."));

        try {
            ObjectWriteResponse objectWriteResponse = minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioInfo.getBucketName())
                    .object(newFileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build());

            if (objectWriteResponse != null) {
                return minioInfo.getEndpoint() + "/" + minioInfo.getBucketName() + "/" + newFileName;
            }
        } catch (Exception e) {
            log.error("上传文件异常: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件完整URL
     * @return 是否删除成功
     */
    @Override
    public boolean deleteFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            log.debug("文件URL为空，跳过删除");
            return false;
        }

        // 检查是否是本系统上传的文件
        if (!fileUrl.contains("manager/")) {
            log.debug("文件不是本系统上传的，跳过删除: {}", fileUrl);
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

    /**
     * 从完整URL中提取MinIO对象名称
     */
    private String extractObjectName(String url) {
        String bucketName = minioInfo.getBucketName();
        int bucketIndex = url.indexOf(bucketName);
        if (bucketIndex != -1) {
            return url.substring(bucketIndex + bucketName.length() + 1);
        }
        // 如果找不到bucket，尝试找manager/
        int managerIndex = url.indexOf("manager/");
        if (managerIndex != -1) {
            return url.substring(managerIndex);
        }
        return url;
    }
}
