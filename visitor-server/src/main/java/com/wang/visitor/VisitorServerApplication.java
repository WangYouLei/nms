package com.wang.visitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(scanBasePackages = {
        "com.wang.visitor",
        "com.wang.common"
})
@EntityScan("com.wang.pojo")
public class VisitorServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisitorServerApplication.class, args);
    }
}