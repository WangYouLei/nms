package com.wang.aiserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {
        "com.wang.aiserver",
        "com.wang.common"
})
@MapperScan("com.wang.aiserver.mapper")
@EnableFeignClients
@EnableDiscoveryClient
public class AiServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiServerApplication.class, args);
    }
}
