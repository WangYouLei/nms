package com.wang.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;


@SpringBootApplication(scanBasePackages = {
        "com.wang.manage",
        "com.wang.common"
})
@EntityScan("com.wang.pojo")
public class ManageServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManageServerApplication.class, args);
    }
}