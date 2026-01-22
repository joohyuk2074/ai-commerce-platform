package com.example.spartallm.chat.controller.dto.response;

import com.example.spartallm.chat.application.dto.result.LlmModelListResult;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LlmModelListResponse(
    String object,

    List<LlmModelSummary> data
) {
    public static LlmModelListResponse of(LlmModelListResult llmModeListResult) {
        List<LlmModelSummary> modelSummaries = llmModeListResult.models().stream()
            .map(model -> new LlmModelSummary(
                model.id(),
                "model",
                model.created(),
                model.ownedBy()
            ))
            .toList();

        return new LlmModelListResponse("list", modelSummaries);
    }

    public record LlmModelSummary(
        String id,

        String object,

        long created,

        @JsonProperty("owned_by")
        String ownedBy
    ) {
    }
}
