package com.example.spartallm.chat.application.dto.result;

import com.example.spartallm.chat.domain.model.LlmModelInfo;

import java.util.List;

public record LlmModelListResult(
    List<LlmModelInfo> models
) {
    public static LlmModelListResult of(List<LlmModelInfo> models) {
        return new LlmModelListResult(models);
    }
}
