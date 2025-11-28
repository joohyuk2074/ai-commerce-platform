package com.example.spartallm.embedding.application.dto.result;

import java.util.List;

public record EmbeddingResult(
    List<EmbeddingData> embeddings,
    String model,
    UsageData usage
) {
    public record EmbeddingData(
        List<Double> embedding,
        Integer index
    ) {}

    public record UsageData(
        Integer promptTokens,
        Integer totalTokens
    ) {}
}