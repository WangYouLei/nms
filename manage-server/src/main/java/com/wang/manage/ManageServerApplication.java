package com.wang.manage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "com.wang")
@EnableConfigurationProperties
@MapperScan("com.wang.manage.mapper")
public class ManageServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManageServerApplication.class, args);
    }
}