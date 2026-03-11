package com.wang.visitor.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    //TODO manage-server模块和visitor-server模块的两个文件上传功能重复了，
    // 可以提取出来放到一个新模块中（或者弄成一个工具类），
    // 目前先两个模块都写，等后面这种通用功能比较多在同一,
    // 为什么不放到common模块中？因为就算放到那个模块中也要添加feign依赖，不然两个模块之间不会相互调用
    private MinioInfo minioInfo;

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
