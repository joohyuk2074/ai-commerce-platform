package com.example.spartallm.embedding.domain.port;

import com.example.spartallm.embedding.domain.model.EmbeddingRequestModel;
import com.example.spartallm.embedding.domain.model.EmbeddingResponseModel;

public interface LlmEmbeddingPort {

    EmbeddingResponseModel embed(EmbeddingRequestModel request);

    boolean supports(String modelName);

    String getModelName();
}