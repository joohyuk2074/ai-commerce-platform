package com.example.spartallm.embedding.controller.dto.request;

import com.example.spartallm.embedding.application.dto.query.EmbeddingQuery;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record EmbeddingRequest(
    @NotEmpty
    List<String> input,

    @NotBlank
    String model,

    String encodingFormat
) {
    public EmbeddingRequest {
        if (encodingFormat == null) {
            encodingFormat = "float";
        }
    }

    public EmbeddingQuery toEmbeddingQuery() {
        return new EmbeddingQuery(
            input,
            model,
            encodingFormat
        );
    }
}