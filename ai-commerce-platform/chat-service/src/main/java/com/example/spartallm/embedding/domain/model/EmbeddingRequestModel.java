package com.example.spartallm.embedding.domain.model;

import java.util.List;

public record EmbeddingRequestModel(
    List<String> input,
    String modelName
) {
    public static EmbeddingRequestModel of(List<String> input, String modelName) {
        return new EmbeddingRequestModel(input, modelName);
    }
}