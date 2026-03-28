package com.wang.novel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {
        "com.wang.novel",
        "com.wang.common"
})
@EntityScan("com.wang.pojo")
@EnableFeignClients
@EnableDiscoveryClient
@MapperScan("com.wang.novel.mapper")
public class NovelServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(NovelServerApplication.class, args);
    }
}
