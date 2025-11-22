package com.example.spartallm.chat.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ClaudeChatConfig {

    // TODO: 설정 추가
    @Bean(name = "claude-sonnet-4-5-20250929")
    public AnthropicChatModel caludeSonnet4_5ChatModel() {
        AnthropicApi build = AnthropicApi.builder()
            .apiKey("temp")
            .build();
        return AnthropicChatModel.builder()
            .anthropicApi(build)
            .build();
    }
}
