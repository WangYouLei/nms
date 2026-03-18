package com.wang.commonserver.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    private final MinioInfo minioInfo;

    public MinioConfig(MinioInfo minioInfo) {
        this.minioInfo = minioInfo;
    }

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioInfo.getEndpoint())
                .credentials(minioInfo.getAccessKey(), minioInfo.getSecretKey())
                .build();
    }
}