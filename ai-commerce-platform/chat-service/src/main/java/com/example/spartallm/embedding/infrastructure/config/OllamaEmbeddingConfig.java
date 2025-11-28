package com.example.spartallm.embedding.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@Setter
@ConfigurationProperties(prefix = "spring.ai.ollama.embedding")
@RequiredArgsConstructor
public class OllamaEmbeddingConfig {

    private final OllamaApi ollamaApi;

    @Bean(name = "nomic-embed-text")
    public OllamaEmbeddingModel nomicEmbedTextModel() {
        OllamaEmbeddingOptions options = OllamaEmbeddingOptions.builder()
            .model("nomic-embed-text")
            .build();

        log.info("Ollama Embedding 모델 생성: {}", options.getModel());

        return OllamaEmbeddingModel.builder()
            .ollamaApi(ollamaApi)
            .defaultOptions(options)
            .build();
    }

    @Bean(name = "mxbai-embed-large")
    public OllamaEmbeddingModel mxbaiEmbedLargeModel() {
        OllamaEmbeddingOptions options = OllamaEmbeddingOptions.builder()
            .model("mxbai-embed-large")
            .build();

        log.info("Ollama Embedding 모델 생성: {}", options.getModel());

        return OllamaEmbeddingModel.builder()
            .ollamaApi(ollamaApi)
            .defaultOptions(options)
            .build();
    }
}