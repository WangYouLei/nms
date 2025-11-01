package com.wang.visitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.wang.visitor",
        "com.wang.common"
})
@EntityScan("com.wang.pojo")
@EnableJpaRepositories("com.wang.visitor.repository")
public class VisitorServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisitorServerApplication.class, args);
    }
}