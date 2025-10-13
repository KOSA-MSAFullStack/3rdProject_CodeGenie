package com.codegenie.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.client.ChatClient;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        // Starter가 제공한 Builder로 기본 모델/옵션을 물린 ChatClient 생성
        return builder.build();
    }
}
