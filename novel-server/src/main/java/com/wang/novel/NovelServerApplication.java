package com.wang.novel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = {
        "com.wang.novel",
        "com.wang.common",
        "com.wang.commonserver"
})
@EntityScan("com.wang.pojo")
@MapperScan("com.wang.novel.mapper")
public class NovelServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(NovelServerApplication.class, args);
    }
}
