package com.wang.manager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "com.wang.manager",
        "com.wang.common"
})
@EntityScan("com.wang.pojo")
@EnableFeignClients(basePackages = "com.wang.common.feign")
@EnableDiscoveryClient
@EnableScheduling
@MapperScan("com.wang.manager.mapper")
public class ManagerServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManagerServerApplication.class, args);
    }
}