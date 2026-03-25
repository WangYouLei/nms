package com.wang.commonserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "model")
@Component
@Data
public class ModelConfig {

    private String apiKey;

    private String text1;

    private String voice1;
}
