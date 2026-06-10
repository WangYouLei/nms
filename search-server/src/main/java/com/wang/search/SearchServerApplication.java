package com.wang.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.wang.search",
        "com.wang.common"
}, exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients(basePackages = "com.wang.common.feign")
@EnableDiscoveryClient
@EnableElasticsearchRepositories(basePackages = "com.wang.search.repository")
public class SearchServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SearchServerApplication.class, args);
    }
}
