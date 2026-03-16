package com.wang.novel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "minio")
@Component
@Data
public class MinioInfo {

    //访问地址
    private String endpoint;

    //账号
    private String accessKey;

    //密码
    private String secretKey;

    //存储桶名称
    private String bucketName;
}