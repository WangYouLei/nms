package com.wang.comment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = {
        "com.wang.comment",
        "com.wang.common",
        "com.wang.commonserver"
})
@EntityScan("com.wang.pojo")
@MapperScan("com.wang.comment.mapper")
public class CommentServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommentServerApplication.class, args);
    }
}