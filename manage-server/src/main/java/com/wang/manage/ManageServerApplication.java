package com.wang.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.wang.manage",
        "com.wang.common"
})
@EntityScan("com.wang.novelmanagementsystem.pojo")
@EnableJpaRepositories("com.wang.manage.repository")
@EnableConfigurationProperties
public class ManageServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManageServerApplication.class, args);
    }
}