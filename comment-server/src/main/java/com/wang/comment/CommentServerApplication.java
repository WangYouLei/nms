package com.wang.comment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {
        "com.wang.comment",
        "com.wang.common"
})
@EntityScan("com.wang.pojo")
@EnableFeignClients(basePackages = "com.wang.common.feign")
@EnableDiscoveryClient
@MapperScan("com.wang.comment.mapper")
public class CommentServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommentServerApplication.class, args);
    }
}