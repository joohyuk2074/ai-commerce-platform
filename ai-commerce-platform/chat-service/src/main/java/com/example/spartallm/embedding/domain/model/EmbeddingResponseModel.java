package com.example.spartallm.embedding.domain.model;

import java.util.List;

public record EmbeddingResponseModel(
    List<EmbeddingVector> embeddings,
    String model,
    Integer totalTokens
) {
    public record EmbeddingVector(
        List<Double> vector,
        Integer index
    ) {}
}