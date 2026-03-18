package com.wang.visitor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;


@SpringBootApplication(scanBasePackages = {
        "com.wang.visitor",
        "com.wang.common",
        "com.wang.commonserver"
})
@EntityScan("com.wang.pojo")
@MapperScan("com.wang.visitor.mapper")
public class VisitorServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisitorServerApplication.class, args);
    }
}