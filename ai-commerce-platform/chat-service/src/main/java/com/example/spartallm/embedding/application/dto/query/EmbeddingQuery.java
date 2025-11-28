package com.example.spartallm.embedding.application.dto.query;

import java.util.List;

public record EmbeddingQuery(
    List<String> input,
    String model,
    String encodingFormat
) {
}