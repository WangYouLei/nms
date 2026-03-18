package com.wang.manage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;


@SpringBootApplication(scanBasePackages = {
        "com.wang.manage",
        "com.wang.common",
        "com.wang.commonserver"
})
@EntityScan("com.wang.pojo")
@MapperScan("com.wang.manage.mapper")
public class AuthorServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthorServerApplication.class, args);
    }
}