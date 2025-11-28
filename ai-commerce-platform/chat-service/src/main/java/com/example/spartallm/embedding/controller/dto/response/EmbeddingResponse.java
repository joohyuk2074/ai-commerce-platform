package com.example.spartallm.embedding.controller.dto.response;

import com.example.spartallm.embedding.application.dto.result.EmbeddingResult;

import java.util.List;

public record EmbeddingResponse(
    String object,
    List<EmbeddingData> data,
    String model,
    UsageInfo usage
) {
    public static EmbeddingResponse of(EmbeddingResult result) {
        List<EmbeddingData> data = result.embeddings().stream()
            .map(e -> new EmbeddingData("embedding", e.embedding(), e.index()))
            .toList();

        UsageInfo usage = new UsageInfo(
            result.usage().promptTokens(),
            result.usage().totalTokens()
        );

        return new EmbeddingResponse(
            "list",
            data,
            result.model(),
            usage
        );
    }

    public record EmbeddingData(
        String object,
        List<Double> embedding,
        Integer index
    ) {}

    public record UsageInfo(
        Integer promptTokens,
        Integer totalTokens
    ) {}
}