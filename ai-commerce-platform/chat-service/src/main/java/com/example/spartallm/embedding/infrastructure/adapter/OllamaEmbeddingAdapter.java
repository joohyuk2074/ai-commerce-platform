package com.example.spartallm.embedding.infrastructure.adapter;

import com.example.spartallm.embedding.domain.model.EmbeddingRequestModel;
import com.example.spartallm.embedding.domain.model.EmbeddingResponseModel;
import com.example.spartallm.embedding.domain.port.LlmEmbeddingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OllamaEmbeddingAdapter implements LlmEmbeddingPort {

    private final Map<String, EmbeddingModel> embeddingClients;

    public OllamaEmbeddingAdapter(
        @org.springframework.beans.factory.annotation.Qualifier("nomic-embed-text") OllamaEmbeddingModel nomicModel,
        @org.springframework.beans.factory.annotation.Qualifier("mxbai-embed-large") OllamaEmbeddingModel mxbaiModel
    ) {
        this.embeddingClients = new java.util.HashMap<>();
        this.embeddingClients.put("nomic-embed-text", nomicModel);
        this.embeddingClients.put("mxbai-embed-large", mxbaiModel);
        log.info("OllamaEmbeddingAdapter initialized with {} models", embeddingClients.size());
    }

    @Override
    public EmbeddingResponseModel embed(EmbeddingRequestModel request) {
        EmbeddingModel embeddingModel = embeddingClients.get(request.modelName());

        if (embeddingModel == null) {
            throw new IllegalArgumentException("지원하지 않는 Ollama 임베딩 모델입니다: " + request.modelName());
        }

        EmbeddingRequest embeddingRequest = new EmbeddingRequest(request.input(), null);
        EmbeddingResponse embeddingResponse = embeddingModel.call(embeddingRequest);

        List<EmbeddingResponseModel.EmbeddingVector> vectors = embeddingResponse.getResults().stream()
            .map(result -> {
                float[] output = result.getOutput();
                List<Double> doubleList = new java.util.ArrayList<>(output.length);
                for (float f : output) {
                    doubleList.add((double) f);
                }
                return new EmbeddingResponseModel.EmbeddingVector(doubleList, result.getIndex());
            })
            .toList();

        int totalTokens = request.input().stream()
            .mapToInt(s -> s.split("\\s+").length)
            .sum();

        return new EmbeddingResponseModel(vectors, request.modelName(), totalTokens);
    }

    @Override
    public boolean supports(String modelName) {
        return embeddingClients.containsKey(modelName);
    }

    @Override
    public String getModelName() {
        return "ollama-embedding";
    }
}