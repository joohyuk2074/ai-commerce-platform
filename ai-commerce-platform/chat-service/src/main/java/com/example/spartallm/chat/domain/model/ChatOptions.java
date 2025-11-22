package com.example.spartallm.chat.domain.model;

public record ChatOptions(
    Double temperature,
    Integer maxTokens,
    Double topP,
    Integer topK
) {
    public static ChatOptions of(Double temperature, Integer maxTokens) {
        return new ChatOptions(temperature, maxTokens, null, null);
    }
}
