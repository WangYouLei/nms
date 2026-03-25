package com.wang.commonserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.wang.commonserver",
        "com.wang.common"
})
@MapperScan("com.wang.commonserver.mapper")
public class CommonServerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CommonServerApplication.class, args);
    }
}