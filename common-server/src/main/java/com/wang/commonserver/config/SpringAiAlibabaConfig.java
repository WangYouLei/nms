package com.wang.commonserver.config;


import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class SpringAiAlibabaConfig {
    // 模型名称常量定义
    private final ModelConfig modelConfig;

    public SpringAiAlibabaConfig(ModelConfig modelConfig) {
        this.modelConfig = modelConfig;
    }

    @Bean(name = "textModel1")
    public ChatModel textModel1()
    {
        return DashScopeChatModel.builder()
                .dashScopeApi(DashScopeApi.builder()
                        .apiKey(modelConfig.getApiKey())
                        .build())
                .defaultOptions(
                        DashScopeChatOptions.builder().withModel(modelConfig.getText1()).build()
                )
                .build();
    }

    @Bean(name = "voiceModel1")
    public ChatModel voiceModel1()
    {
        return DashScopeChatModel.builder().dashScopeApi(DashScopeApi.builder()
                        .apiKey(modelConfig.getApiKey())
                        .build())
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withModel(modelConfig.getVoice1())
                                .build()
                )
                .build();
    }

    @Bean(name = "textClient1")
    public ChatClient textClient1(@Qualifier("textModel1") ChatModel deepSeek)
    {
        return ChatClient.builder(deepSeek)
                .defaultOptions(ChatOptions.builder()
                        .model(modelConfig.getText1())
                        .build())
                .build();
    }


    @Bean(name = "voiceClient1")
    public ChatClient voiceClient1(@Qualifier("voiceModel1") ChatModel qwen)
    {
        return ChatClient.builder(qwen)
                .defaultOptions(ChatOptions.builder()
                        .model(modelConfig.getVoice1())
                        .build())
                .build();
    }
}
