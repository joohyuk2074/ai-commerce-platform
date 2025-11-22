package com.example.spartallm.chat.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
@Setter
@ConfigurationProperties(prefix = "spring.ai.ollama")
@RequiredArgsConstructor
public class OllamaConfig {

    private String baseUrl;

    @Bean
    public OllamaApi ollamaApi() {
        log.info("=== OllamaApi 생성 ===");
        log.info("Base URL: {}", baseUrl);

        // ✅ Builder 패턴 사용
        return OllamaApi.builder()
            .baseUrl(baseUrl)
            .build();
    }

    @Primary
    @Bean(name = "qwen3bChatModel")
    public OllamaChatModel qwen3bChatModel(OllamaApi ollamaApi) {
        OllamaChatOptions options = OllamaChatOptions.builder()
            .model("qwen2.5:3b")
            .temperature(0.7)
            .numPredict(1000)
            .topK(40)
            .topP(0.9)
            .repeatPenalty(1.1)
            .build();

        log.info("Ollama 모델(3B): {}", options.getModel());

        return OllamaChatModel.builder()
            .ollamaApi(ollamaApi)
            .defaultOptions(options)
            .build();
    }

    @Bean(name = "qwen05bChatModel")
    public OllamaChatModel qwen05bChatModel(OllamaApi ollamaApi) {
        OllamaChatOptions options = OllamaChatOptions.builder()
            .model("qwen2.5:0.5b")
            .temperature(0.5)
            .numPredict(512)
            .topK(40)
            .topP(0.9)
            .repeatPenalty(1.1)
            .build();

        log.info("Ollama 모델(0.5B): {}", options.getModel());

        return OllamaChatModel.builder()
            .ollamaApi(ollamaApi)
            .defaultOptions(options)
            .build();
    }

    // 7B
    @Bean(name = "qwen7bChatModel")
    public OllamaChatModel qwen7bChatModel(OllamaApi ollamaApi) {
        OllamaChatOptions options = OllamaChatOptions.builder()
            .model("qwen2.5:7b")
            .temperature(0.7)
            .numPredict(1500)
            .topK(40)
            .topP(0.9)
            .repeatPenalty(1.1)
            .build();

        log.info("Ollama 모델(7B): {}", options.getModel());

        return OllamaChatModel.builder()
            .ollamaApi(ollamaApi)
            .defaultOptions(options)
            .build();
    }
}
