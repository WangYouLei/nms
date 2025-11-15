package com.wang.manage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "com.wang")
@EnableConfigurationProperties
/*@MapperScan("com.wang.manage.mapper")*/
@EnableTransactionManagement//开启事务
public class ManageServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManageServerApplication.class, args);
    }
}